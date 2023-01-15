package org.ogreg.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;

public class PerfTest {

	public static void main(String[] args) throws InterruptedException, IOException {
		Options options = new Options.Builder()
				.server("nats://%s:4222".formatted(System.getProperty("nats.host", "mynats.sandbox")))
				.build();

		String testName = System.getenv("TEST_NAME");

		switch (testName) {
			case "simple":
				simpleProducerConsumer(options);
				return;
			default:
				throw new UnsupportedOperationException("Unknown test: " + testName);
		}
	}

	private static void simpleProducerConsumer(Options options) throws InterruptedException, IOException {
		for (int i = 0; i < 5; i++) {

			try (Connection nc = Nats.connect(options)) {
				// Consumer
				int iterations = 100000;

				long before = System.currentTimeMillis();
				CountDownLatch latch = new CountDownLatch(iterations);
				Dispatcher d = nc.createDispatcher(m -> latch.countDown());
				d.subscribe("my.topic");

				// Producer
				byte[] message = new byte[4096];
				Arrays.fill(message, (byte) 1);
				for (int c = 0; c < iterations; c++) {
					nc.publish("my.topic", message);
				}

				if (latch.await(3, SECONDS)) {
					long duration = System.currentTimeMillis() - before;
					System.out.printf("Sent/received %d messages in %d ms (%d/s)%n", iterations, duration, iterations / duration * 1000);
				} else {
					System.out.printf("Timed out, received only %d/%d messages!%n", latch.getCount(), iterations);
					return;
				}
			}
		}
	}
}

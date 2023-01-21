package org.ogreg.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class PerfTest {

	public static void main(String[] args) throws Exception {
		System.err.printf("ENV: %s%n", System.getenv());

		Options options = new Options.Builder()
				.server("nats://%s:4222".formatted(System.getProperty("nats.host", "mynats.sandbox")))
				.build();

		String testName = System.getenv("TEST_NAME");

		switch (testName) {
			case "simple":
				simpleProducerConsumer(options);
				return;
			case "stream":
				jetstreamTest(options);
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
					System.out.printf("Sent/received %d messages in %d ms (%d/s)%n", iterations, duration, iterations * 1000L / duration);
				} else {
					System.out.printf("Timed out, received only %d/%d messages!%n", latch.getCount(), iterations);
					return;
				}
			}
		}
	}

	private static void jetstreamTest(Options options) throws IOException, InterruptedException, JetStreamApiException, TimeoutException {
		int iterations = 10000;

		try (Connection nc = Nats.connect(options)) {
			JetStreamManagement jsm = nc.jetStreamManagement();
			try {
				if (jsm.deleteStream("mystream")) {
					System.out.printf("Deleted 'mystream'%n");
				}
			} catch (Exception e) { // ignored
			}

			StreamConfiguration sc = StreamConfiguration.builder()
					.name("mystream")
					.subjects("mystream.topic")
					.storageType(StorageType.File)
					.replicas(2)
					.retentionPolicy(RetentionPolicy.Limits)
					.maxMessages(iterations)
					.maxMsgSize(4096)
					.build();
			jsm.addStream(sc);

			JetStream js = nc.jetStream();

			// Producer
			long before = System.currentTimeMillis();
			byte[] message = new byte[4096];
			Arrays.fill(message, (byte) 1);
			for (int c = 0; c < iterations; c++) {
				js.publish("mystream.topic", message);
			}
			long duration = System.currentTimeMillis() - before;
			System.out.printf("Published %d messages in %d ms (%d/s)%n", iterations, duration, iterations * 1000L / duration);
		}

		// Ephemeral consumer (reading the stream fully)
		try (Connection nc = Nats.connect(options)) {
			JetStream js = nc.jetStream();

			long before = System.currentTimeMillis();
			Dispatcher dispatcher = nc.createDispatcher();
			CountDownLatch latch = new CountDownLatch(iterations);
			PushSubscribeOptions pso = PushSubscribeOptions.builder()
					.ordered(true)
					.build();
			js.subscribe("mystream.topic", dispatcher, m -> latch.countDown(), false, pso);

			if (latch.await(10, SECONDS)) {
				long duration = System.currentTimeMillis() - before;
				System.out.printf("Read %d messages in %d ms (%d/s)%n", iterations, duration, iterations * 1000L / duration);
			} else {
				System.out.printf("Timed out, received only %d/%d messages!%n", latch.getCount(), iterations);
			}
		}
	}
}

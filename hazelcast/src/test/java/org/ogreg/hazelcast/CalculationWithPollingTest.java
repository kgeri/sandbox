package org.ogreg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CalculationWithPollingTest {
	private static final Logger log = LoggerFactory.getLogger(CalculationWithPollingTest.class);

	private static HazelcastInstance node1;
	private static HazelcastInstance node2;
	private static HazelcastInstance client;

	@BeforeAll
	public static void beforeAll() {
		node1 = startNode();
		node2 = startNode();

		ClientConfig c = new ClientConfig();
		c.setClusterName("testcluster");
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		client = HazelcastClient.newHazelcastClient(c);
	}

	private static HazelcastInstance startNode() {
		Config c = new Config();
		c.setClusterName("testcluster");
		c.getProperties().put("hazelcast.logging.type", "slf4j");
		c.getProperties().put("hazelcast.wait.seconds.before.join", "0");

		HazelcastInstance hz = Hazelcast.newHazelcastInstance(c);
		new CalculatorServiceWithPolling<CalculationTask>(hz, "WorkQueue", 2, 100);
		return hz;
	}

	@Test
	public void calculationsAreDeDuplicatedAndDistributedByPartitionKey() throws InterruptedException {
		IMap<CalculationTask, Long> workQueue = client.getMap("WorkQueue");
		Random rnd = new Random(0);
		for (int i = 0; i < 1000; i++) {
			workQueue.put(new VerifiedCalculationTask("test", rnd.nextInt(10)), System.currentTimeMillis());
		}
		Thread.sleep(1000); // Waiting for the TaskQueue to fill
		while (!workQueue.isEmpty()) { // Waiting for the TaskQueue to drain
			Thread.sleep(100);
		}

		assertThat(VerifiedCalculationTask.calculatedSequences).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		assertThat(VerifiedCalculationTask.calculationCount).hasValueLessThan(100 * 10 / 2);
		log.info("Redundancy: {}x", VerifiedCalculationTask.calculationCount.doubleValue() / 10.0);
	}

	@AfterAll
	public static void afterAll() {
		node1.shutdown();
		node2.shutdown();
	}

	private static class VerifiedCalculationTask extends CalculationTask {
		private static final Set<Integer> calculatedSequences = Collections.synchronizedSet(new HashSet<>());
		private static final AtomicInteger calculationCount = new AtomicInteger(0);

		VerifiedCalculationTask(String name, int sequence) {
			super(name, sequence);
		}

		@Override
		public void run() {
			calculatedSequences.add(sequence);
			calculationCount.incrementAndGet();
			super.run();
		}
	}
}

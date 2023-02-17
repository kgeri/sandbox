package org.ogreg.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
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

public class CalculationTest extends HazelcastTestSupport {
	private static final Logger log = LoggerFactory.getLogger(CalculationTest.class);

	private static HazelcastInstance client;

	@BeforeAll
	public static void beforeAll() {
		HazelcastInstance node1 = startNode("testcluster");
		HazelcastInstance node2 = startNode("testcluster");

		new CalculatorService<CalculationTask>(node1, "WorkQueue", 2);
		new CalculatorService<CalculationTask>(node2, "WorkQueue", 2);

		client = newClient("testcluster");
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
		assertThat(VerifiedCalculationTask.calculationCount).hasValueLessThan(1000 / 2);
		log.info("Redundancy: {}x", VerifiedCalculationTask.calculationCount.doubleValue() / 10.0);
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

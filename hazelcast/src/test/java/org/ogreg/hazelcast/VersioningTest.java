package org.ogreg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ogreg.hazelcast.VersioningMapStore.OptimisticLockingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class VersioningTest {
	private static final int ITERATIONS = 1000;
	private static final int BATCH_SIZE = 100;

	private static HazelcastInstance node1;
	private static HazelcastInstance node2;
	private static HazelcastInstance client;

	private final Map<String, Position> sampleBatch1 = Map.of(
			"ACCOUNT1", new Position(1),
			"ACCOUNT2", new Position(2),
			"ACCOUNT3", new Position(2)
	);
	private final Map<String, Position> sampleBatch2 = Map.of(
			"ACCOUNT1", new Position(2),
			"ACCOUNT2", new Position(1),
			"ACCOUNT3", new Position(2)
	);
	private final List<Map<String, Position>> randomBatches = IntStream.range(0, ITERATIONS)
			.mapToObj(i -> randomBatch(BATCH_SIZE)).toList();

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

		MapStoreConfig msc = c.getMapConfig("PositionsWithMapStore").getMapStoreConfig();
		msc.setEnabled(true);
		msc.setImplementation(new VersioningMapStore());

		return Hazelcast.newHazelcastInstance(c);
	}

	@Test
	void versioningMapStoreCorrectness() {
		IMap<String, Position> positions = client.getMap("PositionsWithMapStore");
		try {
			positions.putAll(sampleBatch1);
			positions.putAll(sampleBatch2);
			fail("Expected OptimisticLockingException");
		} catch (OptimisticLockingException e) {
			// TODO _which_ exception we get from the bunch is probably random?
			assertThat(e.getLocalizedMessage()).matches("Outdated: Position\\[version=\\d], version <= 2");
		}

		assertThat(positions.values()).contains(
				new Position(2),
				new Position(2),
				new Position(2));
	}

	@Test
	void versioningMapStorePerformance() {
		IMap<String, Position> positions = client.getMap("PositionsWithMapStore");
		long before = System.currentTimeMillis();
		for (Map<String, Position> batch : randomBatches) {
			try {
				positions.putAll(batch);
			} catch (OptimisticLockingException e) {
				// Ignore
			}
		}
		long duration = System.currentTimeMillis() - before;
		System.out.printf("MapStore throughput: %d/s%n", ITERATIONS * BATCH_SIZE * 1000L / duration);
	}

	@Test
	void versioningEntryProcessorCorrectness() {
		IMap<String, Position> positions = client.getMap("Positions");
		positions.executeOnKeys(sampleBatch1.keySet(), new VersioningEntryProcessor<>(sampleBatch1));
		positions.executeOnKeys(sampleBatch2.keySet(), new VersioningEntryProcessor<>(sampleBatch2));
		// no error expected

		assertThat(positions.values()).contains(
				new Position(2),
				new Position(2),
				new Position(2));
	}

	@Test
	void versioningEntryProcessorPerformance() {
		IMap<String, Position> positions = client.getMap("PositionsWithMapStore");
		long before = System.currentTimeMillis();
		for (Map<String, Position> batch : randomBatches) {
			positions.executeOnKeys(batch.keySet(), new VersioningEntryProcessor<>(batch));
		}
		long duration = System.currentTimeMillis() - before;
		System.out.printf("EntryProcessor throughput: %d/s%n", ITERATIONS * BATCH_SIZE * 1000L / duration);
	}

	@AfterAll
	public static void afterAll() {
		node1.shutdown();
		node2.shutdown();
	}

	private Map<String, Position> randomBatch(int count) {
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		Map<String, Position> batch = new HashMap<>();
		for (int i = 0; i < count; i++) {
			batch.put("ACCOUNT" + i, new Position(rnd.nextInt(100)));
		}
		return batch;
	}
}

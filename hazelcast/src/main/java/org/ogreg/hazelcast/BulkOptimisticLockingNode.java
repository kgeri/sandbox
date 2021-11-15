package org.ogreg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.ogreg.hazelcast.VersioningMapStore.OptimisticLockingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

public class BulkOptimisticLockingNode {
	private static final Logger log = LoggerFactory.getLogger(BulkOptimisticLockingNode.class);
	private static final String CLUSTER_NAME = "bulkOL";

	public BulkOptimisticLockingNode() {
		Config c = new Config();
		c.setClusterName(CLUSTER_NAME);
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		MapStoreConfig msc = c.getMapConfig("PositionsWithMapStore").getMapStoreConfig();
		msc.setEnabled(true);
		msc.setImplementation(new VersioningMapStore());

		Hazelcast.newHazelcastInstance(c);
	}

	@SuppressWarnings("InstantiationOfUtilityClass")
	public static void main(String[] args) {
		new BulkOptimisticLockingNode();
		new BulkOptimisticLockingNode();

		ClientConfig c = new ClientConfig();
		c.setClusterName(CLUSTER_NAME);
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		HazelcastInstance hz = HazelcastClient.newHazelcastClient(c);

		// Simple test data that's guaranteed to fail the version check
		Map<String, Position> sampleBatch1 = Map.of(
				"ACCOUNT1", new Position(1),
				"ACCOUNT2", new Position(2),
				"ACCOUNT3", new Position(1)
		);
		Map<String, Position> sampleBatch2 = Map.of(
				"ACCOUNT1", new Position(2),
				"ACCOUNT2", new Position(2),
				"ACCOUNT3", new Position(2)
		);

		optimisticLockingWithEntryProcessor(hz, sampleBatch1);
		optimisticLockingWithEntryProcessor(hz, sampleBatch2);

//		optimisticLockingWithMapStoreHack(hz, sampleBatch1);
//		optimisticLockingWithMapStoreHack(hz, sampleBatch2);

		System.exit(0);
	}

	private static void optimisticLockingWithEntryProcessor(HazelcastInstance hz, Map<String, Position> batch) {
		IMap<String, Position> positions = hz.getMap("Positions");
		positions.executeOnKeys(batch.keySet(), new VersioningEntryProcessor<>(batch));
	}

	private static void optimisticLockingWithMapStoreHack(HazelcastInstance hz, Map<String, Position> batch) {
		IMap<String, Position> positions = hz.getMap("PositionsWithMapStore");
		try {
			positions.putAll(batch);
		} catch (OptimisticLockingException e) {
			log.info("Optimistic locking failure: " + e.getLocalizedMessage());
		}
		log.info("Positions: " + positions.values());
	}

	record Position(int version) implements Serializable, Versioned {
	}
}

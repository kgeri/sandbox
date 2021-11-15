package org.ogreg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStoreAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

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

		optimisticLockingWithMapStoreHack(hz);

		System.exit(0);
	}

	private static void optimisticLockingWithMapStoreHack(HazelcastInstance hz) {
		IMap<String, Position> positions = hz.getMap("PositionsWithMapStore");
		try {
			positions.putAll(Map.of(
					"ACCOUNT1", new Position(1),
					"ACCOUNT2", new Position(2),
					"ACCOUNT3", new Position(1)
			));

			positions.putAll(Map.of(
					"ACCOUNT1", new Position(2),
					"ACCOUNT2", new Position(2),
					"ACCOUNT3", new Position(2)
			));
		} catch (RuntimeException e) {
			log.error("Failed to put batch", e);
		}
		log.info("Positions: " + positions.values());
	}

	interface Versioned {
		int version();
	}

	record Position(int version) implements Serializable, Versioned {
	}

	static class VersioningMapStore extends MapStoreAdapter<Object, Versioned> implements MapLoaderLifecycleSupport {
		private IMap<Object, Versioned> map;

		@Override
		public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
			map = hazelcastInstance.getMap(mapName);
		}

		@Override
		public void store(Object key, Versioned value) {
			Versioned oldValue = map.get(key);
			if (oldValue != null && value.version() <= oldValue.version()) {
				throw new OptimisticLockingException("Outdated: " + value + ", version <= " + oldValue.version());
			}
			log.info("store: {}={}", key, value);
		}

		@Override
		public void destroy() {
		}
	}

	public static class OptimisticLockingException extends RuntimeException implements Serializable {
		public OptimisticLockingException(String message) {
			super(message);
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			return this;
		}
	}
}

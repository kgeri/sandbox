package org.ogreg.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStoreAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;

class VersioningMapStore extends MapStoreAdapter<Object, Versioned> implements MapLoaderLifecycleSupport {
	private static final Logger log = LoggerFactory.getLogger(VersioningMapStore.class);

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
		log.debug("store: {}={}", key, value);
	}

	@Override
	public void destroy() {
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

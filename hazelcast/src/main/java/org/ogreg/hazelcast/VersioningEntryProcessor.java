package org.ogreg.hazelcast;

import com.hazelcast.map.EntryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

public class VersioningEntryProcessor<K extends Serializable, V extends Versioned & Serializable> implements EntryProcessor<K, V, Void>, Serializable {
	private static final Logger log = LoggerFactory.getLogger(VersioningEntryProcessor.class);

	private final Map<K, V> batch;

	public VersioningEntryProcessor(Map<K, V> batch) {
		this.batch = batch;
	}

	@Override
	public Void process(Entry<K, V> entry) {
		V oldValue = entry.getValue();
		V newValue = batch.get(entry.getKey());

		if (newValue == null) {
			return null; // We don't have a value for this entry (what are we doing here, then?), or it was set to null (TODO: do we want to support deletes?)
		} else if (oldValue == null) {
			entry.setValue(newValue); // There was no old value
		} else if (oldValue.version() < newValue.version()) {
			entry.setValue(newValue); // The new version is larger than the old one - updating
		} else {
			log.info("Outdated: {}, version <= {}", newValue, oldValue.version());
		}
		return null;
	}
}

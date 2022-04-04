package org.ogreg.hazelcast;

import com.hazelcast.partition.PartitionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class CalculationTask implements Runnable, PartitionAware<String>, Serializable {
	private static final Logger log = LoggerFactory.getLogger(CalculationTask.class);

	private final String name;
	private final int sequence;

	public CalculationTask(String name, int sequence) {
		this.name = name;
		this.sequence = sequence;
	}

	@Override
	public String getPartitionKey() {
		return name + sequence;
	}

	@Override
	public void run() {
		log.info("Executing: {}-{}", name, sequence);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}

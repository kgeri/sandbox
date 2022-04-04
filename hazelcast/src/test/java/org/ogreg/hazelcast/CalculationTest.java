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

public class CalculationTest {

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

		MapStoreConfig msc = c.getMapConfig("PositionsWithMapStore").getMapStoreConfig();
		msc.setEnabled(true);
		msc.setImplementation(new VersioningMapStore());

		HazelcastInstance hz = Hazelcast.newHazelcastInstance(c);
		new CalculatorService<CalculationTask>(hz, 2);
		return hz;
	}

	@Test
	public void calculationsAreDeDuplicatedAndDistributedByPartitionKey() throws InterruptedException {
		IMap<CalculationTask, Long> workQueue = client.getMap("WorkQueue");
		for (int j = 0; j < 100; j++) {
			for (int i = 0; i < 10; i++) {
				workQueue.put(new CalculationTask("test", i), System.currentTimeMillis());
			}
		}
		Thread.sleep(1000);
	}

	@AfterAll
	public static void afterAll() {
		node1.shutdown();
		node2.shutdown();
	}
}

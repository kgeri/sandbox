package org.ogreg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.AfterAll;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Helper base class for tests that fire up a Hazelcast cluster.
 * <p>
 * Also takes care of cleaning up the nodes in {@link #terminateAll()}.
 */
public abstract class HazelcastTestSupport {
	private static final List<HazelcastInstance> nodes = new ArrayList<>();
	private static final List<HazelcastInstance> clients = new ArrayList<>();

	public static HazelcastInstance startNode(String clusterName) {
		return startNode(clusterName, config -> {
		});
	}

	public static HazelcastInstance startNode(String clusterName, Consumer<Config> configurer) {
		Config c = new Config();
		c.setClusterName(clusterName);
		c.getProperties().put("hazelcast.logging.type", "slf4j");
		c.getProperties().put("hazelcast.wait.seconds.before.join", "0"); // This is necessary for a faster startup
		c.getProperties().put("hazelcast.phone.home.enabled", "false");
		// c.getProperties().put("hazelcast.partition.count", "3"); // To make debugging easier...

		// Reducing thread counts to make debugging easier
		c.getProperties().put("hazelcast.event.thread.count", "1");
		c.getProperties().put("hazelcast.operation.generic.thread.count", "1");
		c.getProperties().put("hazelcast.operation.priority.generic.thread.count", "1");
		c.getProperties().put("hazelcast.operation.thread.count", "1");
		c.getProperties().put("hazelcast.clientengine.thread.count", "1");
		c.getProperties().put("hazelcast.clientengine.query.thread.count", "1");
		c.getProperties().put("hazelcast.io.thread.count", "1");
		c.getProperties().put("hazelcast.operation.response.thread.count", "0"); // "If set to 0, the response threads are bypassed and the response handling is done on the IO threads."

		c.getMetricsConfig().setEnabled(false);

		configurer.accept(c);

		HazelcastInstance hz = Hazelcast.newHazelcastInstance(c);
		nodes.add(hz);
		return hz;
	}

	public static HazelcastInstance newClient(String clusterName) {
		return newClient(clusterName, c -> {
		});
	}

	public static HazelcastInstance newClient(String clusterName, Consumer<ClientConfig> configurer) {
		ClientConfig c = new ClientConfig();
		c.setClusterName(clusterName);
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		configurer.accept(c);
		HazelcastInstance hz = HazelcastClient.newHazelcastClient(c);
		clients.add(hz);
		return hz;
	}

	@AfterAll
	public static void terminateAll() {
		clients.forEach(HazelcastInstance::shutdown);
		nodes.forEach(HazelcastInstance::shutdown);
	}
}

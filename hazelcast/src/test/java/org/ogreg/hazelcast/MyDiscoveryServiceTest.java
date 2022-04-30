package org.ogreg.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;

public class MyDiscoveryServiceTest {

	@Test
	void testDiscovery() {
		Config c = new Config();
		c.setClusterName("mydiscovery");
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		c.getNetworkConfig().getJoin().getDiscoveryConfig().setDiscoveryServiceProvider(MyDiscoveryService::new);

		HazelcastInstance n1 = Hazelcast.newHazelcastInstance(c);
		HazelcastInstance n2 = Hazelcast.newHazelcastInstance(c);

		n1.shutdown();
		n2.shutdown();
	}
}

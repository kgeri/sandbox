package org.ogreg.hazelcast;

import com.hazelcast.config.Config;
import org.junit.jupiter.api.Test;

public class MyDiscoveryServiceTest extends HazelcastTestSupport {

	private static void configurer(Config c) {
		c.getNetworkConfig().getJoin().getDiscoveryConfig().setDiscoveryServiceProvider(MyDiscoveryService::new);
	}

	@Test
	void testDiscovery() {
		startNode("mydiscovery", MyDiscoveryServiceTest::configurer);
		startNode("mydiscovery", MyDiscoveryServiceTest::configurer);

		// TODO verify that discovery happened

		terminateAll();
	}
}

package org.ogreg.hazelcast;

import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.integration.DiscoveryService;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceSettings;
import com.hazelcast.spi.discovery.multicast.MulticastDiscoveryStrategy;

import java.util.Map;

import static java.util.Collections.emptyMap;

public class MyDiscoveryService implements DiscoveryService {

	private final DiscoveryStrategy strategy;

	public MyDiscoveryService(DiscoveryServiceSettings settings) {
		DiscoveryNode discoveryNode = settings.getDiscoveryNode();
		ILogger logger = settings.getLogger();
		strategy = new MulticastDiscoveryStrategy(discoveryNode, logger, emptyMap());
	}

	@Override
	public void start() {
		strategy.start();
	}

	@Override
	public Iterable<DiscoveryNode> discoverNodes() {
		return strategy.discoverNodes();
	}

	@Override
	public void destroy() {
		strategy.destroy();
	}

	@Override
	public Map<String, String> discoverLocalMetadata() {
		return emptyMap();
	}
}

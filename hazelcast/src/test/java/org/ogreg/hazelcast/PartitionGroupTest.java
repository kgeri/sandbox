package org.ogreg.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceProxy;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static com.hazelcast.config.PartitionGroupConfig.MemberGroupType.ZONE_AWARE;
import static com.hazelcast.spi.partitiongroup.PartitionGroupMetaData.PARTITION_GROUP_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

public class PartitionGroupTest extends HazelcastTestSupport {
	private HazelcastInstance z1n1;
	private HazelcastInstance z1n2;
	private HazelcastInstance z2n1;
	private HazelcastInstance z2n2;
	private IMap<String, Integer> testMap;

	// This configuration is responsible for tagging the nodes with a PARTITION_GROUP_ZONE
	private static Consumer<Config> zonedConfig(String zoneName) {
		return (Config c) -> {
			MemberAttributeConfig mac = new MemberAttributeConfig();
			mac.setAttribute(PARTITION_GROUP_ZONE, zoneName);
			c.setMemberAttributeConfig(mac);
			c.getPartitionGroupConfig().setEnabled(true);
			c.getPartitionGroupConfig().setGroupType(ZONE_AWARE);
		};
	}

	@BeforeEach
	void setUp() {
		z1n1 = startNode("mypartitiongroups", zonedConfig("zone1"));
		z1n2 = startNode("mypartitiongroups", zonedConfig("zone1"));
		z2n1 = startNode("mypartitiongroups", zonedConfig("zone2"));
		z2n2 = startNode("mypartitiongroups", zonedConfig("zone2"));

		{
			IMap<String, Integer> testMap = z2n2.getMap("Test");
			for (int i = 0; i < 1000; i++) {
				testMap.put(String.valueOf(i), i);
			}
		}
		testMap = z1n1.getMap("Test");
		assertThat(testMap.values()).hasSize(1000);
	}

	@Test
	void oneNodeCanBeLostBecauseOfBackups() {
		kill(z2n1);
		assertThat(testMap.values()).hasSize(1000);
	}

	@Test
	void aZoneCanBeLostBecauseOfZoneAwarePartitioning() {
		kill(z2n1, z2n2);
		assertThat(testMap.values()).hasSize(1000);
	}

	@Test
	void losingTooManyNodesCausesDataLoss() {
		kill(z1n2, z2n1, z2n2);
		assertThat(testMap.values()).hasSizeLessThan(1000);
	}

	@AfterEach
	void tearDown() {
		terminateAll();
	}

	private void kill(HazelcastInstance... nodes) {
		// Note: shutting down servers in advance, because apparently even terminate() allows some re-balancing to happen!
		for (HazelcastInstance node : nodes) {
			((HazelcastInstanceProxy) node).getOriginal().node.server.shutdown();
		}
		for (HazelcastInstance node : nodes) {
			node.getLifecycleService().terminate();
		}
	}
}

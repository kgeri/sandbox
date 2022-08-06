package org.ogreg.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceProxy;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.hazelcast.config.PartitionGroupConfig.MemberGroupType.ZONE_AWARE;
import static com.hazelcast.spi.partitiongroup.PartitionGroupMetaData.PARTITION_GROUP_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

public class PartitionGroupTest {
	private final List<HazelcastInstance> nodes = new ArrayList<>();

	private HazelcastInstance z1n1;
	private HazelcastInstance z1n2;
	private HazelcastInstance z2n1;
	private HazelcastInstance z2n2;
	private IMap<String, Integer> testMap;

	@BeforeEach
	void setUp() {
		z1n1 = startNode("zone1");
		z1n2 = startNode("zone1");
		z2n1 = startNode("zone2");
		z2n2 = startNode("zone2");

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
		nodes.forEach(HazelcastInstance::shutdown);
	}

	private HazelcastInstance startNode(String zone) {
		Config c = new Config();
		c.setClusterName("mypartitiongroups");
		c.getProperties().put("hazelcast.logging.type", "slf4j");
		c.getProperties().put("hazelcast.wait.seconds.before.join", "0");

		MemberAttributeConfig mac = new MemberAttributeConfig();
		mac.setAttribute(PARTITION_GROUP_ZONE, zone);
		c.setMemberAttributeConfig(mac);
		c.getPartitionGroupConfig().setEnabled(true);
		c.getPartitionGroupConfig().setGroupType(ZONE_AWARE);

		HazelcastInstance hz = Hazelcast.newHazelcastInstance(c);
		nodes.add(hz);
		return hz;
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

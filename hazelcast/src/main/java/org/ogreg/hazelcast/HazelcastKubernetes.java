package org.ogreg.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.KubernetesConfig;
import com.hazelcast.core.Hazelcast;

public class HazelcastKubernetes {

	public static void main(String[] args) {
		Config c = new Config();
		c.setClusterName("test-cluster");
		c.getIntegrityCheckerConfig().setEnabled(false);
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		JoinConfig jc = c.getNetworkConfig().getJoin();
		jc.getMulticastConfig().setEnabled(false);

		KubernetesConfig kc = jc.getKubernetesConfig();
		kc.setEnabled(true);
		kc.setProperty("service-dns", "test-cluster.sandbox.svc.cluster.local");

		Hazelcast.newHazelcastInstance(c);
	}
}

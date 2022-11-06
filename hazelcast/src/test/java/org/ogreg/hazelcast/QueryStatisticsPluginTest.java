package org.ogreg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.impl.query.Query;
import com.hazelcast.query.Predicates;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class QueryStatisticsPluginTest {
	private static HazelcastInstance node;
	private static HazelcastInstance client;

	@BeforeAll
	static void beforeAll() {
		QueryStatisticsPlugin.install();
		node = startNode();

		ClientConfig c = new ClientConfig();
		c.setClusterName("testcluster");
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		client = HazelcastClient.newHazelcastClient(c);
	}

	@Test
	void queryStatisticsAreLogged() {
		IMap<String, Person> map = client.getMap("TestMap");

		map.put("A", new Person("Jim", 32));
		map.put("B", new Person("Joe", 47));
		map.put("C", new Person("Jane", 53));

		Collection<Person> results = map.values(Predicates.greaterThan("age", 40));
		assertThat(results).hasSize(2);
		assertThat(QueryStatisticsPlugin.LastQuery)
				.extracting(Query::getPredicate)
				.asString().isEqualTo("age>40");
	}

	@AfterAll
	static void afterAll() {
		node.shutdown();
	}

	private static HazelcastInstance startNode() {
		Config c = new Config();
		c.setClusterName("testcluster");
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		HazelcastInstance hz = Hazelcast.newHazelcastInstance(c);
		return hz;
	}
}

record Person(String name, int age) implements Serializable {
}
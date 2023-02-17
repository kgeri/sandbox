package org.ogreg.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.impl.query.Query;
import com.hazelcast.query.Predicates;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class QueryStatisticsPluginTest extends HazelcastTestSupport {
	private static HazelcastInstance client;

	@BeforeAll
	static void beforeAll() {
		QueryStatisticsPlugin.install();
		startNode("testcluster");
		client = newClient("testcluster");
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
}

record Person(String name, int age) implements Serializable {
}
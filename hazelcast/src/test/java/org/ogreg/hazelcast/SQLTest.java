package org.ogreg.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.sql.SqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;

public class SQLTest {
	private static final Logger log = LoggerFactory.getLogger(SQLTest.class);

	public static void main(String[] args) {
		HazelcastInstance n1 = startNode();
		HazelcastInstance n2 = startNode();
		HazelcastInstance n3 = startNode();

		createMapping(n1);

		HazelcastInstance client = startClient();
		fillWithData(client);
		query(client);

		client.shutdown();
		n3.shutdown();
		n2.shutdown();
		n1.shutdown();
	}

	private static HazelcastInstance startNode() {
		Config c = new Config();
		c.setClusterName("testcluster");
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		NetworkConfig nc = c.getNetworkConfig();
		nc.getJoin().getMulticastConfig().setMulticastTimeoutSeconds(1);

		c.getJetConfig().setEnabled(true);

		return Hazelcast.newHazelcastInstance(c);
	}

	private static HazelcastInstance startClient() {
		ClientConfig c = new ClientConfig();
		c.setClusterName("testcluster");
		c.getProperties().put("hazelcast.logging.type", "slf4j");

		return HazelcastClient.newHazelcastClient(c);
	}

	private static void createMapping(HazelcastInstance hz) {
		SqlService sql = hz.getSql();
		sql.execute("CREATE MAPPING IF NOT EXISTS Products (" +
				" __key VARCHAR," +
				" productId VARCHAR," +
				" issuer VARCHAR," +
				" currency VARCHAR" +
				") TYPE IMap OPTIONS(" +
				"'keyFormat'='varchar', " +
				"'valueFormat' = 'java', " +
				"'valueJavaClass' = 'org.ogreg.hazelcast.Product')");
		sql.execute("CREATE MAPPING IF NOT EXISTS Balances (" +
				" __key VARCHAR," +
				" account VARCHAR," +
				" productId VARCHAR," +
				" amount DECIMAL" +
				") TYPE IMap OPTIONS(" +
				"'keyFormat'='varchar', " +
				"'valueFormat' = 'java', " +
				"'valueJavaClass' = 'org.ogreg.hazelcast.Balance')");
		sql.execute("CREATE INDEX IF NOT EXISTS Balances_productId ON Balances(productId) TYPE HASH");
		sql.execute("CREATE VIEW IF NOT EXISTS EnrichedBalances AS" +
				" select b.account, b.productId, b.amount, p.currency" +
				" from Balances as b join Products as p on b.productId = p.__key");
	}

	private static void fillWithData(HazelcastInstance hz) {
		log.info("Loading data...");
		IMap<String, Balance> balances = hz.getMap("Balances");
		IMap<String, Product> products = hz.getMap("Products");

		Random rnd = ThreadLocalRandom.current();
		putRandom(100000, products, i -> new Product("P" + i, "someone", "EUR"));
		putRandom(1000000, balances, i -> new Balance("A" + rnd.nextInt(1000), "P" + (i % 100000), new BigDecimal(rnd.nextInt())));

		log.info("Balances={}, Products={}", balances.size(), products.size());
	}

	private static void query(HazelcastInstance client) {
		SqlService sql = client.getSql();
		Random rnd = ThreadLocalRandom.current();

		{
			long before = System.currentTimeMillis();
			for (int i = 0; i < 10; i++) {
				sql.execute("select b.account, b.productId, b.amount, p.currency" +
						" from Balances as b join Products as p on b.productId = p.__key" +
						" where b.account = ?" +
						" limit 100", "A" + rnd.nextInt(1000));
			}
			log.info("JOIN SELECT: {} queries/s", 10.0 / (System.currentTimeMillis() - before));
		}

		{
			long before = System.currentTimeMillis();
			for (int i = 0; i < 10; i++) {
				sql.execute("select * from EnrichedBalances as eb" +
						" where eb.account = ?" +
						" limit 100", "A" + rnd.nextInt(1000));
			}
			log.info("VIEW: {} queries/s", 10.0 / (System.currentTimeMillis() - before));
		}
	}

	private static <V extends Keyed> void putRandom(int count, IMap<String, V> target, IntFunction<V> producer) {
		Map<String, V> batch = new HashMap<>();
		for (int i = 0; i < count / 1000; i++) {
			batch.clear();
			for (int j = 0; j < 1000; j++) {
				V value = producer.apply(i * 1000 + j);
				batch.put(value.key(), value);
			}
			target.putAll(batch);
		}
	}
}

interface Keyed {
	String key();
}

record Balance(String account, String productId, BigDecimal amount) implements Serializable, Keyed {
	public String key() {
		return account + productId;
	}
}

record Product(String productId, String issuer, String currency) implements Serializable, Keyed {
	public String key() {
		return productId;
	}
}
package org.ogreg.mongo;

import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoServer {
	private static final Logger log = LoggerFactory.getLogger(MongoServer.class);

	public static void main(String[] args) {
		log.info("System.getenv(): {}", System.getenv());
		Micronaut.run(MongoServer.class, args);
	}
}

package org.ogreg.mongo.fruits;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Context
public class FruitDataLoader {
	private static final Logger log = LoggerFactory.getLogger(FruitDataLoader.class);

	private final FruitRepository fruitRepository;

	public FruitDataLoader(FruitRepository fruitRepository) {
		this.fruitRepository = fruitRepository;
	}

	@PostConstruct
	public void init() {
		fruitRepository.saveAll(List.of(
				new Fruit(null, "apple", "green apple"),
				new Fruit(null, "pear", "yellow pear"),
				new Fruit(null, "orange", "orange orange"),
				new Fruit(null, "banana", "yellow banana")
		));
		log.info("Data loaded successfully");
	}
}

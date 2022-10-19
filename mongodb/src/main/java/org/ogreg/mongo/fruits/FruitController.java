package org.ogreg.mongo.fruits;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;

import java.util.List;
import java.util.Optional;

@Controller("/fruits")
public class FruitController {

	private final FruitRepository fruitRepository;

	public FruitController(FruitRepository fruitRepository) {
		this.fruitRepository = fruitRepository;
	}

	@Get
	Iterable<Fruit> list() {
		return fruitRepository.findAll();
	}

	@Get("/{id}")
	Optional<Fruit> find(@PathVariable String id) {
		return fruitRepository.findById(id);
	}

	@Get("/q")
	Iterable<Fruit> query(@QueryValue @NonNull List<String> names) {
		return fruitRepository.findByNameInList(names);
	}
}

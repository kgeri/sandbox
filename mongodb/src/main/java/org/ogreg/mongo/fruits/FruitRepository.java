package org.ogreg.mongo.fruits;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@MongoRepository
public interface FruitRepository extends CrudRepository<Fruit, String> {

	@NonNull
	Iterable<Fruit> findByNameInList(@NonNull List<String> names);
}

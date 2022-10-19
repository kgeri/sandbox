package org.ogreg.mongo.fruits;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import javax.validation.constraints.NotBlank;

@MappedEntity
public record Fruit(
		@Id @GeneratedValue String id,
		@NonNull @NotBlank String name,
		@Nullable String description
) {
}

package org.ogreg.microstream;

import java.nio.file.Path;
import java.util.List;

public class MyRepositorySample {

	public MyRepositorySample(Path storagePath) {
		try (MyRepository<MyEntity> repo = new MyRepository<>(storagePath)) {
			System.out.printf("After load: %s%n", repo.findAll());
			repo.save(List.of(
					new MyEntity("one"),
					new MyEntity("two"),
					new MyEntity("three")
			));
			System.out.printf("After save: %s%n", repo.findAll());
		}
	}
}

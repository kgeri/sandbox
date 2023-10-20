package org.ogreg.microstream;

import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MyRepository<T> implements AutoCloseable {
	private final EmbeddedStorageManager storageManager;
	private final List<T> root;

	@SuppressWarnings("unchecked")
	public MyRepository(Path storagePath) {
		this.storageManager = EmbeddedStorage.Foundation(storagePath)
				.onConnectionFoundation(cf -> {
					cf.setClassLoaderProvider(ClassLoaderProvider.New(Thread.currentThread().getContextClassLoader()));
				}).start();
		
		if (storageManager.root() == null) {
			root = new ArrayList<>();
			storageManager.setRoot(root);
		} else {
			root = (List<T>) storageManager.root();
		}
	}

	public void save(List<T> entities) {
		root.clear();
		root.addAll(entities);
	}

	public List<T> findAll() {
		return root;
	}

	@Override
	public void close() {
		storageManager.storeRoot();
		storageManager.close();
	}
}

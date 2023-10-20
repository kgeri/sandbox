package org.ogreg.microstream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BackwardsCompatibilityTest {
	private static final Path TEST_STORAGE_PATH = Paths.get("./build/microstream");

	@BeforeEach
	void beforeEach() throws IOException {
		FileUtils.deleteDirectory(TEST_STORAGE_PATH.toFile());
	}

	@Test
	void testCompatibility() {
		runWithClasspath("modelv1", "org.ogreg.microstream.MyRepositorySample");
		runWithClasspath("modelv2", "org.ogreg.microstream.MyRepositorySample");
		runWithClasspath("modelv1", "org.ogreg.microstream.MyRepositorySample");
	}

	private static void runWithClasspath(String module, String sampleClassName) {
		ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
		File moduleClassesDir = new File("build/classes/java/" + module);
		Assertions.assertTrue(moduleClassesDir.exists());

		System.out.printf("Running with classpath: %s%n", moduleClassesDir);
		URL[] urls = new URL[]{
				toURL(moduleClassesDir)
		};
		try (URLClassLoader cl = new URLClassLoader(urls, originalCL)) {
			Thread.currentThread().setContextClassLoader(cl);
			Class<?> sampleClass = cl.loadClass(sampleClassName);
			sampleClass.getConstructor(Path.class).newInstance(TEST_STORAGE_PATH);
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException |
		         IOException e) {
			throw new RuntimeException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(originalCL);
		}
	}

	private static URL toURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}

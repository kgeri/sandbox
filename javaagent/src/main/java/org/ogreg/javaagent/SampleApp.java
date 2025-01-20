package org.ogreg.javaagent;

public class SampleApp {

	public static void main(String[] args) throws InterruptedException {
		while (true) {
			Thread.sleep(5000);
			usedMethod();
		}
	}

	private static void usedMethod() {
		System.err.print(".");
	}

	private static void unUsedMethod() {
		System.err.println("This will never be invoked.");
	}
}

package org.ogreg.simulation;

import java.util.concurrent.ThreadLocalRandom;

abstract class RandomUtil {

	static double jiggle() {
		return (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.000001;
	}
}

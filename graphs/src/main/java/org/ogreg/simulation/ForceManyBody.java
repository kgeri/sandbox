package org.ogreg.simulation;

import java.util.Collection;

// Based on https://github.com/d3/d3-force/blob/main/src/manyBody.js
public class ForceManyBody implements Force {
	private final Body2D[] bodies;
	private double strength;

	public ForceManyBody(Collection<? extends Body2D> bodies, double strength) {
		this.bodies = bodies.toArray(Body2D[]::new);
		this.strength = strength;
	}

	@Override
	public void apply(double alpha) {
		for (int i = 0; i < bodies.length; i++) {
			Body2D s = bodies[i];
			for (int j = 0; j < i; j++) {
				Body2D t = bodies[j];

				double x = t.x + t.vx - s.x - s.vx;
				double y = t.y + t.vy - s.y - s.vy;

				// Randomize direction if coincident
				if (x == 0) x = RandomUtil.jiggle();
				if (y == 0) y = RandomUtil.jiggle();

				double l = x * x + y * y;
				if (l < 1.0) l = Math.sqrt(l); // Limit forces for very close nodes
				
				double w = strength * alpha / l;

				t.vx += x * w;
				t.vy += y * w;
			}
		}
	}
}

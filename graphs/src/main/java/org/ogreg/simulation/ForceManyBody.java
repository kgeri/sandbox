package org.ogreg.simulation;

import java.util.List;

// Based on https://github.com/d3/d3-force/blob/main/src/manyBody.js
public class ForceManyBody<B extends Body2D> implements Force {
	private final List<B> bodies;
	private double strength;

	public ForceManyBody(List<B> bodies, double strength) {
		this.bodies = bodies;
		this.strength = strength;
	}

	@Override
	public void apply(double alpha) {
		for (int i = 0; i < bodies.size(); i++) {
			B s = bodies.get(i);
			for (int j = 0; j < i; j++) {
				B t = bodies.get(j);

				double x = t.x + t.vx - s.x - s.vx;
				double y = t.y + t.vy - s.y - s.vy;

				double l = x * x + y * y;
				double w = strength * alpha / l;
				
				t.vx += x * w;
				t.vy += y * w;
				s.vx -= x * w;
				s.vy -= y * w;
			}
		}
	}
}

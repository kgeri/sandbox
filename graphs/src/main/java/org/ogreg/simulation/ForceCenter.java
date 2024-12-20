package org.ogreg.simulation;

import java.util.Collection;

// Based on https://github.com/d3/d3-force/blob/main/src/center.js
public class ForceCenter<B extends Body2D> implements Force {
	private final Collection<B> bodies;
	private final double cx;
	private final double cy;
	private double strength = 1.0;

	public ForceCenter(Collection<B> bodies, double cx, double cy) {
		this.bodies = bodies;
		this.cx = cx;
		this.cy = cy;
	}

	@Override
	public void apply(double alpha) {
		double sx = 0.0, sy = 0.0;
		int n = bodies.size();

		for (B b : bodies) {
			sx += b.x;
			sy += b.y;
		}

		double dx = (sx / n - cx) * strength;
		double dy = (sy / n - cy) * strength;

		for (B b : bodies) {
			b.x -= dx;
			b.y -= dy;
		}
	}
}

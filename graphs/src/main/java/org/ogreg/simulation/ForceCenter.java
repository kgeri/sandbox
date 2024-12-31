package org.ogreg.simulation;

import java.util.Collection;

// Based on https://github.com/d3/d3-force/blob/main/src/center.js
public class ForceCenter implements Force {
	private final Collection<? extends Body2D> bodies;
	private final double cx;
	private final double cy;
	private final double strength;

	public ForceCenter(Collection<? extends Body2D> bodies, double cx, double cy, double strength) {
		this.bodies = bodies;
		this.cx = cx;
		this.cy = cy;
		this.strength = strength;
	}

	@Override
	public void apply(double alpha) {
		double sx = 0.0, sy = 0.0;
		int n = bodies.size();

		for (Body2D b : bodies) {
			sx += b.x;
			sy += b.y;
		}

		double dx = (sx / n - cx) * strength;
		double dy = (sy / n - cy) * strength;

		for (Body2D b : bodies) {
			b.x -= dx;
			b.y -= dy;
		}
	}
}

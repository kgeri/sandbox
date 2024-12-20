package org.ogreg.simulation;

import java.util.Collection;

// Based on https://github.com/d3/d3-force/blob/main/src/link.js
public class ForceLink<L extends Link<? extends Body2D>> implements Force {
	private final Collection<L> links;
	private final double distance;

	public ForceLink(Collection<L> links, double distance) {
		this.links = links;
		this.distance = distance;
	}

	@Override
	public void apply(double alpha) {
		for (L link : links) {
			Body2D s = link.from();
			Body2D t = link.to();
			double x = t.x + t.vx - s.x - s.vx;
			double y = t.y + t.vy - s.y - s.vy;

			double l = Math.sqrt(x * x + y * y);
			l = (l - distance) / l * alpha * 1; // TODO distances[i]=30, strengths[i]=1
			x *= l;
			y *= l;

			t.vx -= x; // TODO bias[i] = 1
			t.vy -= y;
			s.vx += x;
			s.vy += y;
		}
	}
}

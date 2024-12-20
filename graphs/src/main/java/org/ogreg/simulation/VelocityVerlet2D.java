package org.ogreg.simulation;

import java.util.Collection;
import java.util.List;

/**
 * A simplified <a href="https://en.wikipedia.org/wiki/Verlet_integration">Velocity Verlet</a> simulation based on
 * <a href="https://github.com/d3/d3-force/blob/main/src/simulation.js">d3-force</a> (dt = 1, a = 1).
 *
 * @param <B>
 */
public class VelocityVerlet2D<B extends Body2D> {
	private final Collection<B> bodies;
	private final Collection<Force> forces;

	private final double alphaMin = 0.001;
	private final double alphaDecay;
	private final double friction = 0.6;
	private double alpha = 1.0;

	public VelocityVerlet2D(Collection<B> bodies, List<Force> forces, int steps) {
		this.bodies = bodies;
		this.forces = forces;
		this.alphaDecay = 1 - Math.pow(alphaMin, 1.0 / steps);
	}

	public void simulate() {
		//noinspection StatementWithEmptyBody
		while (step()) {
		}
	}

	public boolean step() {
		alpha += -alpha * alphaDecay;
		for (Force force : forces) {
			force.apply(alpha);
		}

		for (B b : bodies) {
			b.vx = b.vx * friction;
			b.vy = b.vy * friction;
			b.x += b.vx;
			b.y += b.vy;
		}

		return alpha > alphaMin;
	}
}

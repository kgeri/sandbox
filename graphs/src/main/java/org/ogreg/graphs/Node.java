package org.ogreg.graphs;

import org.ogreg.simulation.Body2D;

public class Node extends Body2D {

	public Node(double x, double y) {
		super(x, y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}

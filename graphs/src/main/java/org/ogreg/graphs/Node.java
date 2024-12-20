package org.ogreg.graphs;

public class Node {
	private double x;
	private double y;

	public Node(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void moveTo(double x, double y) {
		this.x = x;
		this.y = y;
	}
}

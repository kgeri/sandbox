package org.ogreg.graphs;// File: ForceDirectedLayout.java

import java.util.List;
import java.util.stream.Stream;

public class ForceDirectedLayout {
	private final int iterations = 1000;
	private final double tolerance = 0.01;
	private final Node[] nodes;

	public ForceDirectedLayout(List<Edge> edges) {
		nodes = edges.stream()
				.flatMap(e -> Stream.of(e.from(), e.to()))
				.distinct()
				.toArray(Node[]::new);
	}

	public void layout() {
		int iter = 0;
		while (iter < iterations && layoutNext()) {
			iter++;
		}
	}

	public boolean layoutNext() {
		double maxChange = 0;

		double[] x = new double[nodes.length];
		double[] y = new double[nodes.length];

		for (int i = 0; i < nodes.length; i++) {
			Node ni = nodes[i];
			double sumX = 0, sumY = 0, weightSum = 0;

			for (int j = 0; j < nodes.length; j++) {
				if (i != j) {
					Node nj = nodes[j];

					double dx = nj.getX() - ni.getX();
					double dy = nj.getY() - ni.getY();
					double dist = Math.sqrt(dx * dx + dy * dy);

					if (dist < 0.001) dist = 0.001; // Prevent divide-by-zero

					double idealDist = 100; // Ideal edge length
					double weight = 1.0 / (idealDist * idealDist); // TODO WTF?

					sumX += weight * (nj.getX() - dx * (idealDist / dist));
					sumY += weight * (nj.getY() - dy * (idealDist / dist));
					weightSum += weight;
				}
			}

			x[i] = sumX / weightSum;
			y[i] = sumY / weightSum;

			double change = Math.sqrt(Math.pow(x[i] - ni.getX(), 2) + Math.pow(y[i] - ni.getY(), 2));
			maxChange = Math.max(maxChange, change);
		}

		for (int i = 0; i < nodes.length; i++) {
			nodes[i].moveTo(x[i], y[i]);
		}

		return maxChange >= tolerance; // Returns false when converged
	}
}

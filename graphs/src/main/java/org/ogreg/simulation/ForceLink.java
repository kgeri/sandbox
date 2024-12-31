package org.ogreg.simulation;

import com.google.common.graph.Graph;

// Based on https://github.com/d3/d3-force/blob/main/src/link.js
public class ForceLink implements Force {
	private final Edge[] edges;
	private final double[] strenghts;
	private final double[] biases;
	private final double distance;

	public ForceLink(Graph<? extends Body2D> graph, double distance) {
		this.edges = graph.edges().stream().map(ep -> new Edge(ep.source(), ep.target())).toArray(Edge[]::new);
		this.strenghts = calculateStrengths(edges, graph);
		this.biases = calculateBiases(edges, graph);
		this.distance = distance;
	}

	@Override
	public void apply(double alpha) {
		for (int i = 0; i < edges.length; i++) {
			Edge edge = edges[i];
			double strength = strenghts[i];
			double bias = biases[i];

			Body2D s = edge.source();
			Body2D t = edge.target();
			double x = t.x + t.vx - s.x - s.vx;
			double y = t.y + t.vy - s.y - s.vy;

			if (x == 0) x = RandomUtil.jiggle();
			if (y == 0) y = RandomUtil.jiggle();

			double l = Math.sqrt(x * x + y * y);
			l = (l - distance) / l * alpha * strength;
			x *= l;
			y *= l;

			t.vx -= x * bias;
			t.vy -= y * bias;
			s.vx += x * (1.0 - bias);
			s.vy += y * (1.0 - bias);
		}
	}

	// See https://d3js.org/d3-force/link#link_strength
	@SuppressWarnings("unchecked")
	private static <N extends Body2D> double[] calculateStrengths(Edge[] edges, Graph<N> graph) {
		double[] strengths = new double[edges.length];
		for (int i = 0; i < edges.length; i++) {
			Edge edge = edges[i];
			strengths[i] = 1.0 / Math.min(
					graph.degree((N) edge.source()),
					graph.degree((N) edge.target())
			);
		}
		return strengths;
	}

	// See https://github.com/d3/d3-force/blob/main/src/link.js#L66
	@SuppressWarnings("unchecked")
	private static <N extends Body2D> double[] calculateBiases(Edge[] edges, Graph<N> graph) {
		double[] biases = new double[edges.length];
		for (int i = 0; i < edges.length; i++) {
			Edge edge = edges[i];
			int sourceDegree = graph.degree((N) edge.source());
			int targetDegree = graph.degree((N) edge.target());
			biases[i] = (double) sourceDegree / (sourceDegree + targetDegree);
		}
		return biases;
	}

	private record Edge(
			Body2D source,
			Body2D target
	) {
	}
}

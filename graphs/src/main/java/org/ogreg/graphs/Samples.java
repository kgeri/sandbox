package org.ogreg.graphs;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

abstract class Samples {

	static Graph<Node> simpleGraph() {
		Node n1 = new Node(300, 400);
		Node n2 = new Node(100, 200);
		Node n3 = new Node(50, 450);
		Node n4 = new Node(200, 200);
		Node n5 = new Node(400, 300);
		Node n6 = new Node(500, 100);

		MutableGraph<Node> g = GraphBuilder.directed().build();
		g.addNode(n1);
		g.addNode(n2);
		g.addNode(n3);
		g.addNode(n4);
		g.addNode(n5);
		g.addNode(n6);

		g.putEdge(n1, n2);
		g.putEdge(n2, n3);
		g.putEdge(n3, n4);
		g.putEdge(n1, n3);
		g.putEdge(n2, n4);
		g.putEdge(n4, n5);
		g.putEdge(n3, n6);
		g.putEdge(n6, n1);

		return g;
	}

	static Graph<Node> randomComplexGraph() {
		return parseFrom("/random_graph.csv");
	}

	private static Graph<Node> parseFrom(String path) {
		MutableGraph<Node> graph = GraphBuilder.directed().build();
		Map<Integer, Node> nodeMap = new HashMap<>();

		try (
				InputStream is = Objects.requireNonNull(Samples.class.getResourceAsStream(path));
				BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF_8))
		) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] nodes = line.split(",");
				if (nodes.length == 2) {
					int source = Integer.parseInt(nodes[0].trim());
					int target = Integer.parseInt(nodes[1].trim());
					Node s = nodeMap.computeIfAbsent(source, n -> new Node(0, 0));
					Node t = nodeMap.computeIfAbsent(target, n -> new Node(0, 0));

					graph.addNode(s);
					graph.addNode(t);
					graph.putEdge(s, t);
				}
			}

			return graph;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

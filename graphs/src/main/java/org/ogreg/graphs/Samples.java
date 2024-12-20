package org.ogreg.graphs;

import java.util.List;

abstract class Samples {

	static Graph simpleGraph() {
		Node n1 = new Node(300, 400);
		Node n2 = new Node(100, 200);
		Node n3 = new Node(50, 450);
		Node n4 = new Node(200, 200);
		Node n5 = new Node(400, 300);
		Node n6 = new Node(500, 100);
		List<Node> nodes = List.of(
				n1, n2, n3, n4, n5, n6
		);

		List<Edge> edges = List.of(
				new Edge(n1, n2),
				new Edge(n2, n3),
				new Edge(n3, n4),
				new Edge(n1, n3),
				new Edge(n2, n4),
				new Edge(n4, n5),
				new Edge(n3, n6),
				new Edge(n6, n1)
		);

		return new Graph(nodes, edges);
	}
}

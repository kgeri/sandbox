package org.ogreg.graphs;

import java.util.List;

abstract class Samples {

	static List<Edge> simpleGraph() {
		Node n1 = new Node(300, 400);
		Node n2 = new Node(301, 401);
		Node n3 = new Node(302, 402);
		Node n4 = new Node(303, 403);
		Node n5 = new Node(304, 404);
		Node n6 = new Node(305, 405);

		return List.of(
				new Edge(n1, n2),
				new Edge(n2, n3),
				new Edge(n3, n4),
				new Edge(n1, n3),
				new Edge(n2, n4),
				new Edge(n4, n5),
				new Edge(n3, n6),
				new Edge(n6, n1)
		);
	}
}

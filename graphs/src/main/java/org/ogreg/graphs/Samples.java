package org.ogreg.graphs;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

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
}

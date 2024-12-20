package org.ogreg.graphs;

import java.util.List;

public record Graph(
		List<Node> nodes,
		List<Edge> edges
) {
}

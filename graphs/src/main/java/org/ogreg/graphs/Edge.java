package org.ogreg.graphs;

import org.ogreg.simulation.Link;

public record Edge(
		Node from,
		Node to
) implements Link<Node> {
}

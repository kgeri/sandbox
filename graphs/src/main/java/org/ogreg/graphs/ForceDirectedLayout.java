package org.ogreg.graphs;

import com.google.common.graph.Graph;
import org.ogreg.simulation.ForceCenter;
import org.ogreg.simulation.ForceLink;
import org.ogreg.simulation.ForceManyBody;
import org.ogreg.simulation.VelocityVerlet2D;

import java.util.List;

public class ForceDirectedLayout extends VelocityVerlet2D<Node> {

	public ForceDirectedLayout(Graph<Node> graph) {
		super(graph.nodes(), List.of(
				new ForceManyBody(graph.nodes(), -30.0),
				new ForceLink(graph, 100.0),
				new ForceCenter(graph.nodes(), 400.0, 400.0)
		), 300);
	}
}

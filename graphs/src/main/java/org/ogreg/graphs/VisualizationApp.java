package org.ogreg.graphs;

import com.google.common.graph.Graph;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualizationApp extends Application {
	private static final Logger log = LoggerFactory.getLogger(VisualizationApp.class);

	public static void main(String[] args) {
		launch(args);
	}

	private final Pane canvas = new Pane();
	private final Graph<Node> graph = Samples.randomComplexGraph();

	@Override
	public void start(Stage stage) {
		initView();
		updateViewCoordinates();

		stage.setTitle("Force-Directed Graph Visualization");
		stage.setScene(new Scene(canvas, 1280, 1024));
		stage.centerOnScreen();
		stage.show();

		new Thread(() -> {
			ForceDirectedLayout fdl = new ForceDirectedLayout(graph);
			int steps = 0;
			while (fdl.step()) {
				Platform.runLater(this::updateViewCoordinates);

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				steps++;
			}

			log.info("Layout complete after {} steps", steps);
		}, "layout").start();
	}

	private void initView() {
		graph.edges().stream()
				.map(ep -> new EdgeView(ep.source(), ep.target()))
				.forEach(ev -> canvas.getChildren().add(ev));
		graph.nodes().stream()
				.map(NodeView::new)
				.forEach(nv -> canvas.getChildren().add(nv));
	}

	private void updateViewCoordinates() {
		for (javafx.scene.Node child : canvas.getChildren()) {
			switch (child) {
				case EdgeView ev -> {
					ev.setStartX(ev.source.getX());
					ev.setStartY(ev.source.getY());
					ev.setEndX(ev.target.getX());
					ev.setEndY(ev.target.getY());
				}
				case NodeView nv -> {
					nv.setCenterX(nv.node.getX());
					nv.setCenterY(nv.node.getY());
				}
				default -> {
				}
			}
		}
	}

	static class NodeView extends Circle {
		private final Node node;

		NodeView(Node node) {
			this.node = node;
			setRadius(10);
			setFill(Color.BLUE);
		}
	}

	static class EdgeView extends Line {
		private final Node source;
		private final Node target;

		EdgeView(Node source, Node target) {
			this.source = source;
			this.target = target;
			setStrokeWidth(2);
		}
	}
}

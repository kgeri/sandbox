package org.ogreg.graphs;

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

import java.util.List;
import java.util.stream.Stream;

public class VisualizationApp extends Application {
	private static final Logger log = LoggerFactory.getLogger(VisualizationApp.class);

	public static void main(String[] args) {
		launch(args);
	}

	private final Pane canvas = new Pane();
	private final List<Edge> edges = Samples.simpleGraph();

	@Override
	public void start(Stage stage) {
		initView();
		updateViewCoordinates();

		stage.setTitle("Force-Directed Graph Visualization");
		stage.setScene(new Scene(canvas, 800, 600));
		stage.centerOnScreen();
		stage.show();

		new Thread(() -> {
			ForceDirectedLayout fdl = new ForceDirectedLayout(edges);
			while (fdl.layoutNext()) {
				Platform.runLater(this::updateViewCoordinates);

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			log.info("Layout complete");
		}, "layout").start();
	}

	private void initView() {
		edges.stream()
				.map(EdgeView::new)
				.forEach(ev -> canvas.getChildren().add(ev));
		edges.stream()
				.flatMap(e -> Stream.of(e.from(), e.to()))
				.distinct()
				.map(NodeView::new)
				.forEach(nv -> canvas.getChildren().add(nv));
	}

	private void updateViewCoordinates() {
		for (javafx.scene.Node child : canvas.getChildren()) {
			switch (child) {
				case EdgeView ev -> {
					ev.setStartX(ev.edge.from().getX());
					ev.setStartY(ev.edge.from().getY());
					ev.setEndX(ev.edge.to().getX());
					ev.setEndY(ev.edge.to().getY());
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
		private final Edge edge;

		EdgeView(Edge edge) {
			this.edge = edge;
			setStrokeWidth(2);
		}
	}
}

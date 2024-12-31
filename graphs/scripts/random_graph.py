#!/usr/bin/python3
# Note: this was generated by ChatGPT. Graph layout and rendering to PNG takes a few mins.

import csv
import random
import networkx as nx
import matplotlib.pyplot as plt

# Parameters
num_nodes = 50
num_edges = 200
output_file = "random_graph.csv"
output_image = "random_graph.png"

# Generate random edges
edges = set()
while len(edges) < num_edges:
    u = random.randint(1, num_nodes)
    v = random.randint(1, num_nodes)
    if u != v:  # Avoid self-loops
        edges.add((u, v) if u < v else (v, u))  # Ensure unique edges

# Write to CSV
with open(output_file, "w", newline="") as file:
    writer = csv.writer(file)
    writer.writerows(edges)

print(f"Random graph with {num_nodes} nodes and {num_edges} edges saved to '{output_file}'.")

# Create a graph object
G = nx.Graph()
G.add_edges_from(edges)

# Visualize the graph
plt.figure(figsize=(50, 50))
pos = nx.spring_layout(G, seed=42)  # Layout for visualization
nx.draw(
    G,
    pos,
    node_size=50,
    edge_color="gray",
    node_color="red",
    alpha=0.7,
    with_labels=False
)

plt.title("Random Graph Visualization")
plt.savefig(output_image, format="png", dpi=100)
plt.close()

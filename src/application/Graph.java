package application;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private static Graph instance;  // Singleton instance
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    private Graph() {}  // Private constructor to prevent direct instantiation

    public static Graph getInstance() {
        if (instance == null) {
            instance = new Graph();
        }
        return instance;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

	public void removeNode(Node node) {
        // Remove all edges connected to this node
        edges.removeIf(e -> e.getFrom().equals(node) || e.getTo().equals(node));
        // Remove the node itself
        nodes.remove(node);
    }
}

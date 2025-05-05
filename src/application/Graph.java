package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Graph {
    private static Graph instance;  // Singleton instance
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    private Graph() {}  // Private constructor to prevent direct instantiation
    
 // For storing the initial state
    private List<Node> initialNodes = new ArrayList<>();
    private List<Edge> initialEdges = new ArrayList<>();

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
	
	public void saveInitialState() {
        initialNodes.clear();
        initialEdges.clear();
        
        // Create deep copies of nodes
        Map<Node, Node> nodeCopies = new HashMap<>();
        for (Node node : nodes) {
            Node copy = node instanceof ImageNode ? 
                new ImageNode((ImageNode) node) : new Node(node);
            nodeCopies.put(node, copy);
            initialNodes.add(copy);
        }
        
        // Create deep copies of edges
        for (Edge edge : edges) {
            Node fromCopy = nodeCopies.get(edge.getFrom());
            Node toCopy = nodeCopies.get(edge.getTo());
            initialEdges.add(new Edge(fromCopy, toCopy, edge.getWeight()));
        }
    }


    // Reset to the initial state
	public void resetToInitialState() {
        nodes.clear();
        edges.clear();
        
        // Reset to the copied nodes and edges
        nodes.addAll(initialNodes);
        edges.addAll(initialEdges);
    }
	
	
}

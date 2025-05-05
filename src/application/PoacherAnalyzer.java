package application;

import java.util.*;

public class PoacherAnalyzer {

    /**
     * Place one poacher on each edge of the map.
     * This implementation calculates the bounding box of the graph based on
     * existing nodes (which are built from the image in ImageGraphBuilder) and
     * adds four new ImageNode objects with type "poacher". (You may want to adjust
     * their cost or other properties as needed.)
     */
    public void enablePoacherAnalysis() {
        Graph graph = Graph.getInstance();
        List<Node> nodes = graph.getNodes();
        if (nodes.isEmpty()) return;
        
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        // Determine bounding box of the current graph
        for (Node node : nodes) {
            double x = node.getX();
            double y = node.getY();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }
        
        // Create one poacher node on each side (center of top, bottom, left, and right)
        ImageNode poacherTop = new ImageNode((minX + maxX) / 2, minY, 0, "poacher");
        ImageNode poacherBottom = new ImageNode(((minX + maxX) / 2)+150, maxY, 0, "poacher");
        ImageNode poacherLeft = new ImageNode(minX, ((minY + maxY) / 2) + 200, 0, "poacher");
        ImageNode poacherRight = new ImageNode(maxX, (minY + maxY) / 2, 0, "poacher");
        
        // Add the poacher nodes to the graph
        graph.addNode(poacherTop);
        graph.addNode(poacherBottom);
        graph.addNode(poacherLeft);
        graph.addNode(poacherRight);
    
	    List<ImageNode> poachers = Arrays.asList(poacherTop, poacherBottom, poacherLeft, poacherRight);
	    for (ImageNode poacher : poachers) {
	    	Node nearest = findNearestNonPoacherNode(poacher,nodes);
	    	if(nearest != null) {
	    		//Connecting the poachernode with its neighbour
	    		Edge edge = new Edge(poacher,nearest);
	    		Edge reverseEgde = new Edge(nearest,poacher);
	    		graph.addEdge(edge);
	    		graph.addEdge(reverseEgde);
	    	}
	    }
    }
    /**
     * Executes a modified Dijkstra algorithm starting from the given node in order
     * to find the shortest path to the nearest critical point (here defined as a forest node).
     * Traversal will avoid nodes marked as water.
     *
     * @param start The starting node (for example, one of the poacher nodes)
     * @return a List of Node objects representing the shortest path found, 
     *         or an empty list if no forest node is reachable.
     */
    public List<Node> executePoacherDijstra(Node start) {
        Graph graph = Graph.getInstance();
        Set<Node> visited = new HashSet<>();
        Map<Node, Double> dist = new HashMap<>();
        Map<Node, Node> prev = new HashMap<>();

        // Initialize distances to all nodes as infinity (or a very high number)
        for (Node node : graph.getNodes()) {
            dist.put(node, Double.MAX_VALUE);
            prev.put(node, null);
        }
        dist.put(start, 0.0);

        // Priority queue wrapper holding nodes with their current distance
        PriorityQueue<NodeWrapper> queue = new PriorityQueue<>(Comparator.comparingDouble(nw -> nw.distance));
        queue.offer(new NodeWrapper(start, 0.0));

        while (!queue.isEmpty()) {
            NodeWrapper current = queue.poll();
            Node curNode = current.node;
            if (visited.contains(curNode)) continue;
            visited.add(curNode);

            // Check if the current node is a forest (our critical point)
            if (curNode instanceof ImageNode && ((ImageNode) curNode).getType().equals("forest")) {
                return reconstructPath(prev, curNode);
            }

            // Explore valid outgoing edges from the current node.
            for (Edge edge : getOutgoingEdges(curNode, graph)) {
                Node neighbor = edge.getTo();
                double alt = dist.get(curNode) + edge.getWeight();
                if (alt < dist.get(neighbor)) {
                    dist.put(neighbor, alt);
                    prev.put(neighbor, curNode);
                    queue.offer(new NodeWrapper(neighbor, alt));
                }
            }
        }
        // No forest was reached
        return new ArrayList<>();
    }

    /**
     * Returns all outgoing edges from the given node that lead to valid terrain.
     * In our example, valid terrain excludes water (so that a poacher does not traverse water).
     *
     * @param node  the starting node
     * @param graph the Graph instance
     * @return a list of valid outgoing edges.
     */
    private List<Edge> getOutgoingEdges(Node node, Graph graph) {
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.getFrom().equals(node) && isValidTerrain(edge.getTo())) {
                edges.add(edge);
            }
        }
        return edges;
    }
    
    /**
     * Checks if the given node represents valid (traversable) terrain.
     * For instance, if the node is an ImageNode with type "water", then it is not valid.
     */
    private boolean isValidTerrain(Node node) {
        if (node instanceof ImageNode) {
            ImageNode imgNode = (ImageNode) node;
            return !imgNode.getType().equals("water");
        }
        return true;
    }

    /**
     * Reconstructs the shortest path from the starting node to the specified target node
     * using the mapping of previous nodes.
     */
    private List<Node> reconstructPath(Map<Node, Node> prev, Node target) {
        List<Node> path = new LinkedList<>();
        for (Node at = target; at != null; at = prev.get(at)) {
            path.add(0, at);
        }
        return path;
    }
    
    private Node findNearestNonPoacherNode(Node poacher, List<Node> allNodes) {
    	Node nearest = null;
    	double minDist = Double.MAX_VALUE;
    	for (Node node: allNodes) {
    		if (node instanceof ImageNode && ((ImageNode) node).getType().equals("poacher"))
    			continue;
    		double dx = poacher.getX()- node.getX();
    		double dy = poacher.getY()- node.getY();
    		double distance = Math.sqrt(dx*dx+dy*dy);
    		if (distance < minDist) {
    			minDist = distance;
    			nearest= node;
    		}
    	}
    	return nearest;
    }

    // Helper class used in the priority queue.
    private static class NodeWrapper {
        Node node;
        double distance;

        NodeWrapper(Node node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}


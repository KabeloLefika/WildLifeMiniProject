package application;

import java.util.*;

public class PathFinder {
    private static final double CONNECTION_DISTANCE = 50; // Pixels
    
	

    private List<Edge> getValidEdges(Node node) {
        List<Edge> validEdges = new ArrayList<>();
        for (Edge edge : Graph.getInstance().getEdges()) {
            if (edge.getFrom().equals(node) && !isWaterNode(edge.getTo())) {
                validEdges.add(edge);
            }
        }
        return validEdges;
    }

    private boolean isWaterNode(Node node) {
        return (node instanceof ImageNode) && 
              ((ImageNode) node).getType().equalsIgnoreCase("water");
    }

    private List<Node> validateAndReconstructPath(Map<Node, Node> previous, Node end) {
        List<Node> path = new ArrayList<>();
        if (!previous.containsKey(end)) return path;

        // Reconstruct path
        Node current = end;
        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);

        // Verify path doesn't contain water nodes
        return path.stream().noneMatch(this::isWaterNode) ? path : Collections.emptyList();
    }
    
    public List<Node> findShortestPath(Node start, Node end) {
        if (start == null || end == null) return Collections.emptyList();
        if (start.equals(end)) return Collections.singletonList(start);

        // Bidirectional search initialization
        Map<Node, Double> forwardDist = new HashMap<>();
        Map<Node, Double> backwardDist = new HashMap<>();
        Map<Node, Node> forwardPrev = new HashMap<>();
        Map<Node, Node> backwardPrev = new HashMap<>();
        
        Set<Node> forwardVisited = new HashSet<>();
        Set<Node> backwardVisited = new HashSet<>();

        PriorityQueue<Node> forwardQueue = new PriorityQueue<>(
            Comparator.comparingDouble(n -> forwardDist.getOrDefault(n, Double.MAX_VALUE))
        );
        PriorityQueue<Node> backwardQueue = new PriorityQueue<>(
            Comparator.comparingDouble(n -> backwardDist.getOrDefault(n, Double.MAX_VALUE))
        );

        // Initialize distances
        forwardDist.put(start, 0.0);
        backwardDist.put(end, 0.0);
        forwardQueue.add(start);
        backwardQueue.add(end);

        Node collisionNode = null;
        double shortestPath = Double.MAX_VALUE;

        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
            // Forward search
            Node fCurrent = forwardQueue.poll();
            forwardVisited.add(fCurrent);

            for (Edge edge : getValidEdges(fCurrent)) {
                Node neighbor = edge.getTo();
                double newDist = forwardDist.get(fCurrent) + edge.getWeight();

                if (!forwardVisited.contains(neighbor) && 
                    newDist < forwardDist.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    
                    forwardDist.put(neighbor, newDist);
                    forwardPrev.put(neighbor, fCurrent);
                    forwardQueue.remove(neighbor);
                    forwardQueue.add(neighbor);
                }
            }

            // Backward search
            Node bCurrent = backwardQueue.poll();
            backwardVisited.add(bCurrent);

            for (Edge edge : getValidEdgesReverse(bCurrent)) {
                Node neighbor = edge.getFrom();  // Reverse direction
                double newDist = backwardDist.get(bCurrent) + edge.getWeight();

                if (!backwardVisited.contains(neighbor) && 
                    newDist < backwardDist.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    
                    backwardDist.put(neighbor, newDist);
                    backwardPrev.put(neighbor, bCurrent);
                    backwardQueue.remove(neighbor);
                    backwardQueue.add(neighbor);
                }
            }

            // Check for collision
            Optional<Node> potentialCollision = forwardVisited.stream()
                .filter(backwardVisited::contains)
                .min(Comparator.comparingDouble(n -> 
                    forwardDist.get(n) + backwardDist.get(n)));

            if (potentialCollision.isPresent()) {
                double currentPathLength = forwardDist.get(potentialCollision.get()) + 
                                          backwardDist.get(potentialCollision.get());
                if (currentPathLength < shortestPath) {
                    shortestPath = currentPathLength;
                    collisionNode = potentialCollision.get();
                }
            }
        }

        return collisionNode != null ? 
            reconstructPath(forwardPrev, backwardPrev, collisionNode) : 
            Collections.emptyList();
    }

    private List<Edge> getValidEdgesReverse(Node node) {
        List<Edge> validEdges = new ArrayList<>();
        for (Edge edge : Graph.getInstance().getEdges()) {
            if (edge.getTo().equals(node) && !isWaterNode(edge.getFrom())) {
                validEdges.add(edge);
            }
        }
        return validEdges;
    }

    private List<Node> reconstructPath(Map<Node, Node> forwardPrev,
                                      Map<Node, Node> backwardPrev,
                                      Node collisionNode) {
        LinkedList<Node> path = new LinkedList<>();
        
        // Build forward path
        Node current = collisionNode;
        while (current != null) {
            path.addFirst(current);
            current = forwardPrev.get(current);
        }
        
        // Build backward path (excluding collision node)
        current = backwardPrev.get(collisionNode);
        while (current != null) {
            path.addLast(current);
            current = backwardPrev.get(current);
        }
        
        // Verify path doesn't contain water nodes
        return path.stream().noneMatch(this::isWaterNode) ? 
            path : 
            Collections.emptyList();
    }

   
}
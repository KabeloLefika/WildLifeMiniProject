package application;

import java.util.*;

public class PathFinder {
 public List<Node> findShortestPath(Node start, Node end) {
     Map<Node, Double> distances = new HashMap<>();
     Map<Node, Node> previous = new HashMap<>();
     PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
     
     // Initialize distances
     for (Node node : Graph.getInstance().getNodes()) {
         distances.put(node, Double.MAX_VALUE);
         previous.put(node, null);
     }
     distances.put(start, 0.0);
     queue.add(start);

     while (!queue.isEmpty()) {
         Node current = queue.poll();
         if (current.equals(end)) break;

         for (Edge edge : getValidEdges(current)) {
             Node neighbor = edge.getTo();
             double newDist = distances.get(current) + edge.getWeight();
             
             if (newDist < distances.get(neighbor)) {
                 distances.put(neighbor, newDist);
                 previous.put(neighbor, current);
                 queue.add(neighbor);
             }
         }
     }

     return reconstructPath(previous, end);
 }

 private List<Edge> getValidEdges(Node node) {
     List<Edge> validEdges = new ArrayList<>();
     for (Edge edge : Graph.getInstance().getEdges()) {
         if (edge.getFrom().equals(node) && isValidTerrain(edge.getTo())) {
             validEdges.add(edge);
         }
     }
     return validEdges;
 }

 private boolean isValidTerrain(Node node) {
     if (node instanceof ImageNode) {
         ImageNode imgNode = (ImageNode) node;
         return !imgNode.getType().equals("water");
     }
     return true;
 }

 private List<Node> reconstructPath(Map<Node, Node> previous, Node end) {
     List<Node> path = new LinkedList<>();
     for (Node at = end; at != null; at = previous.get(at)) {
         path.add(at);
     }
     Collections.reverse(path);
     return path.isEmpty() || path.get(0) != previous.get(end) ? path : Collections.emptyList();
 }
}

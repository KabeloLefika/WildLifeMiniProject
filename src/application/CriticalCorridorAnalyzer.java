package application;

import javafx.scene.paint.Color;
import java.util.*;

public class CriticalCorridorAnalyzer {
    private static final double CRITICAL_RADIUS = 100.0;
    private static final Color CORRIDOR_COLOR = Color.DARKGREEN;
    private static final Color WATER_HIGHLIGHT_COLOR = Color.DARKBLUE;

    public void analyzeCriticalCorridors(Graph graph, GraphView graphView, MapViewer mapViewer) {
        List<Node> forestNodes = new ArrayList<>();
        List<Edge> mstEdges = new ArrayList<>();
        Set<Node> criticalWaterNodes = new HashSet<>();

        // Collect all forest nodes
        for (Node node : graph.getNodes()) {
            if (node instanceof ImageNode && ((ImageNode) node).getType().equals("forest")) {
                forestNodes.add(node);
            }
        }

        if (forestNodes.isEmpty()) {
            graphView.clearHighlights();
            mapViewer.clearHighlights();
            return;
        }

        // Build MST for forests
        mstEdges = buildMST(forestNodes);

        // Find water nodes near MST edges
        for (Edge edge : mstEdges) {
            findNearbyWater(edge.getFrom(), graph.getNodes(), criticalWaterNodes);
            findNearbyWater(edge.getTo(), graph.getNodes(), criticalWaterNodes);
        }

        // Highlight results
        graphView.highlightMST(mstEdges, criticalWaterNodes);
        mapViewer.highlightMST(mstEdges, criticalWaterNodes);
    }

    private List<Edge> buildMST(List<Node> forestNodes) {
        List<Edge> mstEdges = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        // Remove manual weight comparison since Edge calculates it automatically
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(Edge::getWeight));

        Node start = forestNodes.get(0);
        visited.add(start);

        for (Node node : forestNodes) {
            if (!node.equals(start)) {
                // Use 2-argument constructor only
                pq.add(new Edge(start, node));
            }
        }

        while (!pq.isEmpty() && visited.size() < forestNodes.size()) {
            Edge edge = pq.poll();
            Node toNode = edge.getTo();

            if (!visited.contains(toNode)) {
                mstEdges.add(edge);
                visited.add(toNode);

                for (Node node : forestNodes) {
                    if (!visited.contains(node)) {
                        // Use 2-argument constructor
                        pq.add(new Edge(toNode, node));
                    }
                }
            }
        }
        return mstEdges;
    }

    private void findNearbyWater(Node node, List<Node> allNodes, Set<Node> criticalWater) {
        for (Node waterNode : allNodes) {
            if (waterNode instanceof ImageNode && 
                ((ImageNode) waterNode).getType().equals("water") &&
                distanceBetween(node, waterNode) <= CRITICAL_RADIUS) {
                criticalWater.add(waterNode);
            }
        }
    }

    private double distanceBetween(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }
}

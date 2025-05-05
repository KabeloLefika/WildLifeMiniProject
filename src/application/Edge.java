package application;

public class Edge {
    private final Node from;
    private final Node to;
    private final double weight;

    // Existing constructor for automatic weight calculation
    public Edge(Node from, Node to) {
        this.from = from;
        this.to = to;
        this.weight = calculateWeight();
    }

    // New constructor for manual weight specification
    public Edge(Node from, Node to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    private double calculateWeight() {
        double dx = from.getX() - to.getX();
        double dy = from.getY() - to.getY();
        return Math.round(Math.sqrt(dx*dx + dy*dy) * 100.0) / 100.0;
    }

    public Node getFrom() { return from; }
    public Node getTo() { return to; }
    public double getWeight() { return weight; }
}
package application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public class GraphView {
    private final Pane graphPane = new Pane();
    private int imageWidth, imageHeight;
    private Consumer<Node> nodeSelectionCallback;

    // Removed text label maps
    private final Map<Node, Circle> nodeCircles = new HashMap<>();
    private final Map<Edge, Line> edgeLines = new HashMap<>();
    
    private static final Color CORRIDOR_COLOR = Color.DARKGREEN;
    private static final Color WATER_HIGHLIGHT_COLOR = Color.DARKBLUE;

    public void enableNodeSelection(Consumer<Node> callback) {
        this.nodeSelectionCallback = callback;
    }

    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
        update();
    }

    public void update() {
        Graph graph = Graph.getInstance();
        graphPane.getChildren().removeIf(node -> 
            node instanceof Line || node instanceof Circle
        );
        
        double paneWidth = graphPane.getWidth();
        double paneHeight = graphPane.getHeight();
        double scaleX = paneWidth / imageWidth;
        double scaleY = paneHeight / imageHeight;

        // Clear previous mappings
        nodeCircles.clear();
        edgeLines.clear();

        // Draw edges without weights
        for (Edge edge : graph.getEdges()) {
            Line line = createEdgeLine(edge, scaleX, scaleY);
            edgeLines.put(edge, line);
            graphPane.getChildren().add(line);
        }

        // Draw nodes without labels
        for (Node node : graph.getNodes()) {
            Circle circle = createNodeCircle(node, scaleX, scaleY);
            nodeCircles.put(node, circle);
            graphPane.getChildren().add(circle);
        }
    }

    private Line createEdgeLine(Edge edge, double scaleX, double scaleY) {
        double startX = edge.getFrom().getX() * scaleX;
        double startY = edge.getFrom().getY() * scaleY;
        double endX = edge.getTo().getX() * scaleX;
        double endY = edge.getTo().getY() * scaleY;

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.web("#B0BEC5"));
        line.setStrokeWidth(1.0);
        return line;
    }

    private Circle createNodeCircle(Node node, double scaleX, double scaleY) {
        double x = node.getX() * scaleX;
        double y = node.getY() * scaleY;
        
        Circle circle = new Circle(x, y, 5);
        circle.setOnMouseClicked(event -> {
            if (nodeSelectionCallback != null) {
                nodeSelectionCallback.accept(node);
                event.consume();
            }
        });

        updateNodeAppearance(node, circle);
        return circle;
    }
    
    public void highlightStartNode(Node node) {
        Circle circle = nodeCircles.get(node);
        if (circle != null) {
            circle.setFill(Color.GREEN);
        }
    }

    public void highlightEndNode(Node node) {
        Circle circle = nodeCircles.get(node);
        if (circle != null) {
            circle.setFill(Color.RED);
        }
    }

    public void clearNodeHighlights() {
        for (Node node : nodeCircles.keySet()) {
            Circle circle = nodeCircles.get(node);
            updateNodeAppearance(node, circle);
        }
    }

    

    public Pane getView() {
        graphPane.setMinSize(400, 300);
        graphPane.setStyle("-fx-background-color: #263238; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        graphPane.widthProperty().addListener((obs, oldVal, newVal) -> update());
        graphPane.heightProperty().addListener((obs, oldVal, newVal) -> update());
        
        return graphPane;
    }
    
 // In GraphView.java
    public void highlightPath(List<Node> path) {
        clearHighlights();
        
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i+1);
            
            Optional<Edge> connection = Graph.getInstance().getEdges().stream()
                .filter(e -> e.getFrom().equals(current) && e.getTo().equals(next))
                .findFirst();
            
            connection.ifPresent(edge -> {
                Line line = createEdgeLine(edge, 
                    graphPane.getWidth() / imageWidth,
                    graphPane.getHeight() / imageHeight
                );
                line.setStroke(Color.RED);
                line.setStrokeWidth(4);
                line.getStyleClass().add("path-highlight"); // Add this line
                graphPane.getChildren().add(line);
            });
        }
    }
    
    
    
    public void appendPathHighlight(List<Node> path) {
        if (path == null || path.size() < 2) return;
        
        double scaleX = graphPane.getWidth() / imageWidth;
        double scaleY = graphPane.getHeight() / imageHeight;
        
        // Use more visible path styling
        Color pathColor = Color.rgb(255, 165, 0, 0.9); // Brighter orange
        double strokeWidth = 6.0;

        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            Line line = new Line(
                current.getX() * scaleX,
                current.getY() * scaleY,
                next.getX() * scaleX,
                next.getY() * scaleY
            );
            
            line.setStroke(pathColor);
            line.setStrokeWidth(strokeWidth);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.getStyleClass().add("poacher-path");
            graphPane.getChildren().add(line); // Add to end (top of z-order) // Ensure paths are behind nodes
        }
    }
    
    public void updateNodeAppearance(Node node) {
        Circle circle = nodeCircles.get(node);
        if (circle != null) {
            updateNodeAppearance(node, circle);
        }
    }
    
    public void highlightCriticalNode(Node node) {
        double scaleX = graphPane.getWidth() / imageWidth;
        double scaleY = graphPane.getHeight() / imageHeight;
        
        Circle highlight = new Circle(
            node.getX() * scaleX,
            node.getY() * scaleY,
            10,  // Larger radius for critical nodes
            Color.TRANSPARENT
        );
        highlight.setStroke(Color.GOLD);
        highlight.setStrokeWidth(3);
        graphPane.getChildren().add(highlight);
    }
    
    public void highlightMST(List<Edge> mstEdges, Set<Node> criticalWater) {
        clearHighlights();
        
        // Highlight MST edges
        for (Edge edge : mstEdges) {
            Line line = createEdgeLine(edge, 
                graphPane.getWidth() / imageWidth,
                graphPane.getHeight() / imageHeight
            );
            line.setStroke(CORRIDOR_COLOR);
            line.setStrokeWidth(4);
            line.getStyleClass().add("mst-highlight");
            graphPane.getChildren().add(line);
        }

        // Highlight critical water nodes
        double scaleX = graphPane.getWidth() / imageWidth;
        double scaleY = graphPane.getHeight() / imageHeight;
        for (Node node : criticalWater) {
            Circle highlight = new Circle(
                node.getX() * scaleX,
                node.getY() * scaleY,
                7,
                Color.TRANSPARENT
            );
            highlight.setStroke(WATER_HIGHLIGHT_COLOR);
            highlight.setStrokeWidth(2);
            highlight.getStyleClass().add("water-highlight");
            graphPane.getChildren().add(highlight);
        }
    }
    
    private void updateNodeAppearance(Node node, Circle circle) {
        String type = (node instanceof ImageNode) ? 
            ((ImageNode) node).getType() : "default";

        switch (type) {
            case "water":
                circle.setFill(Color.web("#2196F3"));
                break;
            case "forest":
                circle.setFill(Color.web("#43A047"));
                break;
            case "land":
                circle.setFill(Color.web("#A1887F"));
                break;
            case "grass":
                circle.setFill(Color.web("#8BC34A"));
                break;
            case "poacher":
                circle.setFill(Color.web("#FF0000")); 
                break;
            default:
                circle.setFill(Color.web("#FF5722"));
        }

        //circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1.5);
    }

    void clearHighlights() {
        graphPane.getChildren().removeIf(node -> 
            node.getStyleClass().contains("path-highlight") ||
            node.getStyleClass().contains("mst-highlight") ||
            node.getStyleClass().contains("water-highlight") ||
            node.getStyleClass().contains("poacher-path")
        );
    }
   
}

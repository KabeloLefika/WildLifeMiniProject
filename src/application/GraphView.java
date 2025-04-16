package application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

public class GraphView {
    private final Pane graphPane = new Pane();
    private int imageWidth, imageHeight;
    
    private Consumer<Node> nodeSelectionCallback;
    
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
        graphPane.getChildren().clear();
        if (imageWidth <= 0 || imageHeight <= 0) return;

        double paneWidth = graphPane.getWidth();
        double paneHeight = graphPane.getHeight();
        double scaleX = paneWidth / imageWidth;
        double scaleY = paneHeight / imageHeight;

        // Draw edges with weights
        for (Edge edge : Graph.getInstance().getEdges()) {
            Line line = createEdgeLine(edge, scaleX, scaleY);
            Text weightText = createEdgeWeight(edge, scaleX, scaleY);
            graphPane.getChildren().addAll(line, weightText);
        }

        // Draw nodes with labels
        int nodeIndex = 0;
        for (Node node : Graph.getInstance().getNodes()) {
            Circle circle = createNodeCircle(node, scaleX, scaleY);
            Text label = createNodeLabel(node, nodeIndex++, scaleX, scaleY);
            graphPane.getChildren().addAll(circle, label);
        }
        
        System.out.println("Node positions:");
        for (Node node : Graph.getInstance().getNodes()) {
            System.out.println("Node (" + node.getX() + ", " + node.getY() + ")");
        }
    }

    private Line createEdgeLine(Edge edge, double scaleX, double scaleY) {
        double startX = edge.getFrom().getX() * scaleX;
        double startY = edge.getFrom().getY() * scaleY;
        double endX = edge.getTo().getX() * scaleX;
        double endY = edge.getTo().getY() * scaleY;

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.web("#B0BEC5")); // light gray-blue for nice contrast
        line.setStrokeWidth(1.0); // make thinner for a cleaner view
        return line;
    }


    private Text createEdgeWeight(Edge edge, double scaleX, double scaleY) {
        double midX = (edge.getFrom().getX() + edge.getTo().getX()) * scaleX / 2;
        double midY = (edge.getFrom().getY() + edge.getTo().getY()) * scaleY / 2;
        
        Text weightText = new Text(midX, midY, String.format("%.1f", edge.getWeight()));
        weightText.setFill(Color.RED);
        weightText.getStyleClass().add("edge-weight");
        return weightText;
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

        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1.5);
    }


    private Text createNodeLabel(Node node, int index, double scaleX, double scaleY) {
        double x = node.getX() * scaleX + 5;
        double y = node.getY() * scaleY + 5;

        Text label = new Text(x, y, String.valueOf(index));
        label.setFill(Color.web("#212121")); // dark text
        label.setStyle("-fx-font-size: 10px;");
        return label;
    }


    public Pane getView() {
        graphPane.setMinSize(400, 300);
        graphPane.setStyle("-fx-background-color: #263238; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        // Add responsive listeners
        graphPane.widthProperty().addListener((obs, oldVal, newVal) -> update());
      //  graphPane.setStyle("-fx-background-color: #263238;"); // dark blue-gray background

        graphPane.heightProperty().addListener((obs, oldVal, newVal) -> update());
        
        return graphPane;
    }
    
 // Add to GraphView.java
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
                line.getStyleClass().add("path-highlight");
                graphPane.getChildren().add(line);
            });
        }
    }
    
    public void appendPathHighlight(List<Node> path) {
        // Do not clear previous highlights; simply draw additional lines.
        if (path == null || path.size() < 2) return;
        double scaleX = graphPane.getWidth() / (double) imageWidth;
        double scaleY = graphPane.getHeight() / (double) imageHeight;
        
        // Draw the path segments without clearing the pane.
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);
            
            // Find the connection edge (if one exists) between current and next.
            Optional<Edge> connection = Graph.getInstance().getEdges().stream()
                .filter(e -> e.getFrom().equals(current) && e.getTo().equals(next))
                .findFirst();
            
            connection.ifPresent(edge -> {
                Line line = createEdgeLine(edge, scaleX, scaleY);
                line.setStroke(Color.RED);  // You can update the color per path if needed.
                line.setStrokeWidth(4);
                line.getStyleClass().add("path-highlight");
                graphPane.getChildren().add(line);
            });
        }
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

    // Update clearHighlights
    void clearHighlights() {
        graphPane.getChildren().removeIf(node -> 
            node.getStyleClass().contains("path-highlight") ||
            node.getStyleClass().contains("mst-highlight") ||
            node.getStyleClass().contains("water-highlight")
        );
    }

}

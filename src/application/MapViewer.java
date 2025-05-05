package application;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class MapViewer {
    private final ImageView mapView = new ImageView();
    private final Graph graph;
    private final GraphView graphView;
    private final Runnable onLoadComplete;
    private final ProgressBar loadProgress = new ProgressBar();
    private final DoubleProperty zoomLevel = new SimpleDoubleProperty(1.0);
    private double originalWidth, originalHeight;
    private Consumer<Node> nodeSelectionCallback;
    
    private ImageView imageView;
    
    private Circle startMarker, endMarker;
    
    // Overlay pane for drawing the shortest path and instructions on the image
    private final Pane overlay = new Pane();
    private Label instructionLabel; // For instructions
    
    private static final Color CORRIDOR_COLOR = Color.DARKGREEN;
    private static final Color WATER_HIGHLIGHT_COLOR = Color.DARKBLUE;

    public MapViewer(Graph graph, GraphView graphView, Runnable onLoadComplete) {
        this.graph = graph;
        this.graphView = graphView;
        this.onLoadComplete = onLoadComplete;
        setupUI();
    }
    
 // Add these methods
    public double getImageWidth() {
        return mapView.getImage() != null ? mapView.getImage().getWidth() : 0;
    }

    public double getImageHeight() {
        return mapView.getImage() != null ? mapView.getImage().getHeight() : 0;
    }
    
    private void setupUI() {
        mapView.setPreserveRatio(true);
        loadProgress.setPrefWidth(200);
        loadProgress.setVisible(false);
        
        // Initialize zoom controls
        setupZoomHandlers();
    }

    private void setupZoomHandlers() {
        // Mouse wheel zoom for the image
        mapView.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            adjustZoom(zoomFactor);
            event.consume();
        });

        // Track image dimensions
        mapView.imageProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                originalWidth = newImg.getWidth();
                originalHeight = newImg.getHeight();
                updateImageViewSize();
            }
        });
    }
    
    public void enablePathSelection(Consumer<Node> callback) {
        this.nodeSelectionCallback = callback;
        setupNodeClickHandler();
    }
    
    
    public void highlightStartNode(Node node) {
        clearStartMarker();
        double scale = zoomLevel.get();
        startMarker = new Circle(node.getX() * scale, node.getY() * scale, 10, Color.GREEN);
        startMarker.setStroke(Color.BLACK);
        overlay.getChildren().add(startMarker);
    }

    public void highlightEndNode(Node node) {
        clearEndMarker();
        double scale = zoomLevel.get();
        endMarker = new Circle(node.getX() * scale, node.getY() * scale, 10, Color.RED);
        endMarker.setStroke(Color.BLACK);
        overlay.getChildren().add(endMarker);
    }

    public void clearStartMarker() {
        if (startMarker != null) {
            overlay.getChildren().remove(startMarker);
            startMarker = null;
        }
    }

    public void clearEndMarker() {
        if (endMarker != null) {
            overlay.getChildren().remove(endMarker);
            endMarker = null;
        }
    }

    public void clearMarkers() {
        clearStartMarker();
        clearEndMarker();
    }
    private void setupNodeClickHandler() {
        mapView.setOnMouseClicked(event -> {
            if (nodeSelectionCallback != null && mapView.getImage() != null) {
                double scale = zoomLevel.get();
                double clickX = event.getX() / scale;
                double clickY = event.getY() / scale;
                
                Node nearest = findNearestNode(clickX, clickY);
                if (nearest != null) {
                    nodeSelectionCallback.accept(nearest);
                }
            }
        });
    }
    
    private Node findNearestNode(double x, double y) {
        Node closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Node node : Graph.getInstance().getNodes()) {
            double dx = node.getX() - x;
            double dy = node.getY() - y;
            double dist = dx * dx + dy * dy;
            
            if (dist < minDistance) {
                minDistance = dist;
                closest = node;
            }
        }
        
        return minDistance < 100 ? closest : null; // 10px tolerance
    }

    public VBox getView() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);

        Button uploadBtn = new Button("Upload Map Image");
        
        uploadBtn.setStyle(
                "-fx-background-color: #2196F3; " + // Blue background
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " + // Increased padding
                "-fx-background-radius: 8px;" // Rounded corners
            );
        
        uploadBtn.setOnMouseEntered(e -> 
        uploadBtn.setStyle(uploadBtn.getStyle() + 
            "-fx-background-color: #1E88E5;")); // Darker blue on hover
        uploadBtn.setOnMouseExited(e -> 
        uploadBtn.setStyle(uploadBtn.getStyle().replace(
            "#1E88E5", "#2196F3")));
        uploadBtn.setOnAction(e -> handleImageUpload());

        // Create zoom controls separately
        HBox zoomControls = createZoomControls();

        // Use a StackPane to combine the map image, progress bar, overlay (for path drawing and instructions)
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().addAll(mapView, loadProgress, overlay);
        overlay.setPickOnBounds(false); // Allow clicks to pass through
        
        // Center the instruction label at the top of the overlay
        if (instructionLabel == null) {
            instructionLabel = new Label();
            instructionLabel.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 16px;");
            instructionLabel.setVisible(false);
            StackPane.setAlignment(instructionLabel, Pos.TOP_CENTER);
            overlay.getChildren().add(instructionLabel);
        }

        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(600);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(uploadBtn, scrollPane, zoomControls);
        return container;
    }

    private HBox createZoomControls() {
        HBox zoomBox = new HBox(5);
        zoomBox.setAlignment(Pos.CENTER);
        zoomBox.setPadding(new Insets(10));
        
        Button zoomInBtn = new Button("+");
        zoomInBtn.setOnAction(e -> adjustZoom(1.2));
        zoomInBtn.getStyleClass().add("zoom-button");
        
        Button zoomOutBtn = new Button("-");
        zoomOutBtn.setOnAction(e -> adjustZoom(0.8));
        zoomOutBtn.getStyleClass().add("zoom-button");
        
        Button resetBtn = new Button("↺");
        resetBtn.setOnAction(e -> resetZoom());
        resetBtn.getStyleClass().add("zoom-button");
        
        zoomBox.getChildren().addAll(zoomOutBtn, resetBtn, zoomInBtn);
        return zoomBox;
    }

    private void adjustZoom(double factor) {
        zoomLevel.set(Math.max(0.1, Math.min(zoomLevel.get() * factor, 5.0)));
        updateImageViewSize();
    }

    private void resetZoom() {
        zoomLevel.set(1.0);
        updateImageViewSize();
    }

    private void updateImageViewSize() {
        if (mapView.getImage() != null) {
            mapView.setFitWidth(originalWidth * zoomLevel.get());
            mapView.setFitHeight(originalHeight * zoomLevel.get());
        }
    }

 // MapViewer.java
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            loadProgress.setVisible(true);
            Image image = new Image(file.toURI().toString(), true);
            
            image.progressProperty().addListener((obs, old, progress) -> {
                if (progress.doubleValue() == 1.0) {
                    loadProgress.setVisible(false);
                    mapView.setImage(image);
                    graphView.setImageDimensions((int) image.getWidth(), (int) image.getHeight());
                    
                    // Rebuild the graph
                    Graph graph = Graph.getInstance();
                    ImageGraphBuilder.buildGraph(image, graph);
                    graph.saveInitialState(); // Save the initial state
                    
                    onLoadComplete.run();
                }
            });
        }
    }
    
    // This method highlights the path on the original image by drawing lines on the overlay.
    public void highlightPath(List<Node> path) {
        overlay.getChildren().removeIf(node -> node instanceof Line); // Remove previous lines
        if (path == null || path.size() < 2) return;
        double scale = zoomLevel.get();
        for (int i = 0; i < path.size() - 1; i++) {
            Node n1 = path.get(i);
            Node n2 = path.get(i + 1);
            double x1 = n1.getX() * scale;
            double y1 = n1.getY() * scale;
            double x2 = n2.getX() * scale;
            double y2 = n2.getY() * scale;
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.RED);
            line.setStrokeWidth(3);
            overlay.getChildren().add(line);
        }
    }
    
    public void appendPathHighlight(List<Node> path) {
        if (path == null || path.size() < 2) return;
        double scale = zoomLevel.get();
        
        // Draw lines for each segment without clearing the overlay.
        for (int i = 0; i < path.size() - 1; i++) {
            Node n1 = path.get(i);
            Node n2 = path.get(i + 1);
            double x1 = n1.getX() * scale;
            double y1 = n1.getY() * scale;
            double x2 = n2.getX() * scale;
            double y2 = n2.getY() * scale;
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.RED);
            line.setStrokeWidth(3);
            overlay.getChildren().add(line);
        }
    }

    
    // Shows an instructional message over the image with a fade-in and fade-out animation.
    public void showInstruction(String message, int displayMillis) {
        instructionLabel.setText(message);
        instructionLabel.setOpacity(0);
        instructionLabel.setVisible(true);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), instructionLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        PauseTransition pause = new PauseTransition(Duration.millis(displayMillis));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), instructionLabel);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> instructionLabel.setVisible(false));
            fadeOut.play();
        });
        pause.play();
    }
    
    public void clearHighlights() {
        overlay.getChildren().removeIf(node -> node instanceof Line || node instanceof Circle);
    }
    
    public void highlightMST(List<Edge> mstEdges, Set<Node> criticalWater) {
        clearHighlights();
        double scale = zoomLevel.get();

        // Highlight MST edges
        for (Edge edge : mstEdges) {
            Line line = new Line(
                edge.getFrom().getX() * scale,
                edge.getFrom().getY() * scale,
                edge.getTo().getX() * scale,
                edge.getTo().getY() * scale
            );
            line.setStroke(CORRIDOR_COLOR);
            line.setStrokeWidth(4);
            overlay.getChildren().add(line);
        }

        // Highlight critical water nodes
        for (Node node : criticalWater) {
            Circle marker = new Circle(
                node.getX() * scale,
                node.getY() * scale,
                8,
                Color.TRANSPARENT
            );
            marker.setStroke(WATER_HIGHLIGHT_COLOR);
            marker.setStrokeWidth(2);
            overlay.getChildren().add(marker);
        }
    }

	
}
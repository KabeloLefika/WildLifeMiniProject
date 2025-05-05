package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

public class MainDashboard extends BorderPane {
    private VBox sidebar;
    private ProgressBar globalProgress;
    private Graph graph;
    private Button analyzePathsButton;
    private Button toggleSidebarButton;
    private Node startNode, endNode;
    private MapViewer currentMapViewer;
    private GraphView currentGraphView;
    private Label statusLabel; // For showing status messages
    private HBox quickAccessBar; // Holds quick access buttons
    private String currentNodeType;

    // New fields for managing the map/graph container and visibility toggle
    private HBox mapGraphContainer;
    private VBox mapViewerContainer;
    private VBox graphViewContainer;
    private boolean isOriginalImageVisible = true; // tracks original image visibility
    private boolean corridorsHighlighted = false;
    
    private CriticalCorridorAnalyzer corridorAnalyzer = new CriticalCorridorAnalyzer();
    private static final double CONNECTION_DISTANCE = 50;

    public MainDashboard(Stage primaryStage) {
        graph = Graph.getInstance();
        setupUI(primaryStage);
    }

    private void setupUI(Stage primaryStage) {
        sidebar = createSidebar(primaryStage);
        globalProgress = new ProgressBar();
        globalProgress.setVisible(false);
        globalProgress.setPrefWidth(Double.MAX_VALUE);
        
        // Create a top pane that includes the progress bar and the toggle sidebar button
        VBox topPane = createTopPane();
        
        // Main layout structure
        setLeft(sidebar);
        setCenter(createWelcomeMessage());
        setTop(topPane);
        setBottom(createBottomPane());
        
        // Styling the background
        setBackground(new Background(
            new BackgroundFill(Color.web("#F0F3F8"), CornerRadii.EMPTY, Insets.EMPTY)));
        getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }
    
    private VBox createTopPane() {
        VBox topPane = new VBox();
        topPane.setSpacing(5);
        
        // Control bar: contains the toggle sidebar button.
        HBox controlBar = new HBox();
        controlBar.setPadding(new Insets(10));
        controlBar.setSpacing(20);
        controlBar.setAlignment(Pos.CENTER_LEFT);
        
        toggleSidebarButton = new Button("Hide Sidebar");
        toggleSidebarButton.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-background-radius: 5;");
        toggleSidebarButton.setOnAction(e -> toggleSidebar());
        controlBar.getChildren().add(toggleSidebarButton);
        
        // Progress bar container: ensures the progress bar's area is reserved.
        HBox progressContainer = new HBox(globalProgress);
        progressContainer.setPadding(new Insets(5, 10, 5, 10));
        progressContainer.setAlignment(Pos.CENTER);
        
        topPane.getChildren().addAll(controlBar, progressContainer);
        return topPane;
    }
    
    // Toggles the visibility of the sidebar and adjusts the button text.
    private void toggleSidebar() {
        if (getLeft() != null) {
            // Hide the sidebar
            setLeft(null);
            toggleSidebarButton.setText("Show Sidebar");
        } else {
            // Show the sidebar
            setLeft(sidebar);
            toggleSidebarButton.setText("Hide Sidebar");
        }
    }
    
    // Combines the quick access bar and the status label into one pane.
    private BorderPane createBottomPane() {
        BorderPane bottomPane = new BorderPane();
        bottomPane.setPadding(new Insets(20));
        
        // Create quick access buttons and center them.
        HBox quickAccess = createQuickAccess();
        quickAccess.setAlignment(Pos.CENTER);
        bottomPane.setCenter(quickAccess);
        
        //MAking the status more visible through a container at the bottom right
        HBox statusContainer = new HBox();
        statusContainer.getStyleClass().add("status-container");
        statusContainer.setAlignment(Pos.CENTER_RIGHT);

        Label statusTitle = new Label("Status: ");
        statusTitle.getStyleClass().add("status-title");
        
        // Initialize status label if necessary and position it inthe container.
        if (statusLabel == null) {
            statusLabel = new Label("Ready");
            statusLabel.getStyleClass().add("status-message");
        }
        statusContainer.getChildren().addAll(statusTitle, statusLabel);
        // BorderPane.setAlignment(statusLabel, Pos.CENTER_RIGHT);
        bottomPane.setRight(statusContainer);
        
        return bottomPane;
    }

    private VBox createSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(24);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(20));
        sidebar.setAlignment(Pos.TOP_CENTER);

        String[] menuItems = {"map_viewer"};
        for (String item : menuItems) {
            Button btn = createSidebarButton(item);
            if (item.equals("map_viewer")) {
                btn.setOnAction(e -> showMapViewer());
            }
            sidebar.getChildren().add(btn);
        }

     // Logout button with different style
        Button logoutBtn = createSidebarButton("logout");
        logoutBtn.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-background-radius: 5;");
        logoutBtn.getStyleClass().add("logout-button");
        VBox.setMargin(logoutBtn, new Insets(60, 0, 0, 0));
        //sidebar.getChildren().add(logoutBtn);

        //Event Handler toconfirm user is logging out and logs them out
        logoutBtn.setOnAction(e -> {
        	Alert alert = new Alert(AlertType.CONFIRMATION);
        	alert.setTitle("Logout Confirmation");
        	alert.setHeaderText("Are you sure you want to logout?");
        	alert.setContentText("Click OK to confirm logout");
        	
        	Optional<ButtonType> result = alert.showAndWait();
        	//If statement checking whether the button clicked was OK and redirects to welcomwscreen
        	if (result.isPresent() && result.get() == ButtonType.OK) {
        		WelcomeScreen welcome = new WelcomeScreen(primaryStage);
        		Scene welcomeScene = new Scene(welcome,800,600);
        		primaryStage.setScene(welcomeScene);
        	}
        });
        sidebar.getChildren().add(logoutBtn);
        return sidebar;
    }

    private Button createSidebarButton(String item) {
        Button btn = new Button(item.replace("_", " ").toUpperCase());
        btn.getStyleClass().add("nav-button");
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        btn.setContentDisplay(ContentDisplay.LEFT);
        btn.setAlignment(Pos.CENTER_LEFT);
        // Add a default background color for sidebar buttons
        btn.setStyle("-fx-background-color: #7986CB; -fx-text-fill: white; -fx-background-radius: 5;");

        try {
            ImageView icon = new ImageView(new Image(
                getClass().getResourceAsStream("/icons/" + item + ".png")));
            icon.setFitWidth(24);
            icon.setPreserveRatio(true);
            btn.setGraphic(icon);
        } catch (Exception e) {
            System.err.println("Missing icon: " + item);
        }

        return btn;
    }

    private void showMapViewer() {
        globalProgress.setVisible(true);
        
        currentGraphView = new GraphView();
        currentGraphView.getView().setPrefSize(600, 700); // Set preferred size for graph
        
        currentMapViewer = new MapViewer(graph, currentGraphView, () -> {
            globalProgress.setVisible(false);
            currentGraphView.update();
        });

        // Create titled containers with FIXED DIMENSIONS
        mapViewerContainer = new VBox(10);
        mapViewerContainer.setPrefSize(500, 700); // Fixed size for map container
        Label mapTitle = new Label("Original Map");
        
        mapTitle.setStyle(
        	    "-fx-font-size: 23px; " +
        	    "-fx-font-weight: bold; " +
        	    "-fx-text-fill: white; " +
        	    "-fx-padding: 8 16; " + // Added padding
        	    "-fx-background-color: rgba(0, 0, 0, 0.7);" // Semi-transparent background
        	);
       // mapTitle.setStyle("-fx-font-size: 23px; -fx-font-weight: bold;");
        mapViewerContainer.getChildren().addAll(mapTitle, currentMapViewer.getView());
        mapViewerContainer.setAlignment(Pos.TOP_CENTER);

        graphViewContainer = new VBox(10);
        graphViewContainer.setPrefSize(600, 800); // Fixed size for graph container
        Label graphTitle = new Label("Network Graph");
        
        graphTitle.setStyle(
        	    "-fx-font-size: 23px; " +
        	    "-fx-font-weight: bold; " +
        	    "-fx-text-fill: white; " +
        	    "-fx-padding: 8 16; " +
        	    "-fx-background-color: rgba(0, 0, 0, 0.7);"
        	);
        
        //graphTitle.setStyle("-fx-font-size: 23px; -fx-font-weight: bold;");
        graphViewContainer.getChildren().addAll(graphTitle, currentGraphView.getView());
        graphViewContainer.setAlignment(Pos.TOP_CENTER);

        // Container with FIXED layout dimensions
        mapGraphContainer = new HBox(20);
        mapGraphContainer.setPadding(new Insets(20));
        mapGraphContainer.setPrefSize(1040, 740); // 500+500 + 40 (spacing + padding)
        mapGraphContainer.setAlignment(Pos.CENTER);
        mapGraphContainer.getChildren().addAll(mapViewerContainer, graphViewContainer);

        Button toggleImageButton = new Button("Hide Original Image");
        
        
        toggleImageButton.setStyle(
            "-fx-background-color: #2196F3; " + // Blue background
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " + // Increased padding
            "-fx-background-radius: 8px;" // Rounded corners
        );
        
        toggleImageButton.setOnMouseEntered(e -> 
        toggleImageButton.setStyle(toggleImageButton.getStyle() + 
            "-fx-background-color: #1E88E5;")); // Darker blue on hover
    toggleImageButton.setOnMouseExited(e -> 
        toggleImageButton.setStyle(toggleImageButton.getStyle().replace(
            "#1E88E5", "#2196F3")));
        
        toggleImageButton.setOnAction(e -> toggleOriginalImage(toggleImageButton));
        
        HBox buttonContainer = new HBox(toggleImageButton);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10));

        VBox mainContainer = new VBox(10);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().addAll(buttonContainer, mapGraphContainer);

		/*
		 * VBox mainContainer = new VBox(10); mainContainer.setAlignment(Pos.CENTER);
		 * mainContainer.getChildren().addAll(toggleImageButton, mapGraphContainer);
		 */
        
        setCenter(mainContainer);
        showStatus("");
    }
    
    // Toggle the visibility of the original image container
    private void toggleOriginalImage(Button toggleButton) {
        if (isOriginalImageVisible) {
            mapViewerContainer.setVisible(false);
            mapViewerContainer.setManaged(false);
            toggleButton.setText("Show Original Image");
        } else {
            mapViewerContainer.setVisible(true);
            mapViewerContainer.setManaged(true);
            toggleButton.setText("Hide Original Image");
        }
        isOriginalImageVisible = !isOriginalImageVisible;
    }

    
    // Returns the active MapViewer.
    private MapViewer getCurrentMapViewer() {
        return currentMapViewer;
    }

    // Returns the active GraphView.
    private GraphView getCurrentGraphView() {
        return currentGraphView;
    }

    // Updates the status label with a message.
    private void showStatus(String message) {
        if (statusLabel == null) {
            statusLabel = new Label();
        }
        statusLabel.setText(message);
    }
    
    

    // Clears the start and end nodes for path analysis.
    private void resetPathSelection() {
        startNode = null;
        endNode = null;
        currentMapViewer.clearMarkers();
        currentGraphView.clearNodeHighlights();
        showStatus("Path selection reset.");
    }
    
    private GridPane createWelcomeMessage() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(20);
        
        Label welcomeLabel = new Label("Wildlife Conservation Analysis Platform");
        welcomeLabel.getStyleClass().add("welcome-title");
        
        Label instructionLabel = new Label("Select an option from the sidebar to begin");
        instructionLabel.getStyleClass().add("welcome-subtitle");
        
        grid.addRow(0, welcomeLabel);
        grid.addRow(1, instructionLabel);
        return grid;
    }

    private HBox createQuickAccess() {
        quickAccessBar = new HBox(20);
        quickAccessBar.setAlignment(Pos.CENTER);
        quickAccessBar.getStyleClass().add("quick-access-bar");

        Button addNodeBtn = createQuickButton("Add Node", "#4CAF50");
        addNodeBtn.setOnMouseEntered(e -> 
        addNodeBtn.setStyle(addNodeBtn.getStyle() + 
            "-fx-background-color: #1E88E5;")); // Darker blue on hover
        addNodeBtn.setOnMouseExited(e -> 
        addNodeBtn.setStyle(addNodeBtn.getStyle().replace(
            "#1E88E5", "#2196F3")));
        
        addNodeBtn.setOnAction(e -> switchToNoteMode());

        Button findPathsButton = createQuickButton("Find Paths", "#2196F3");
        
        findPathsButton.setOnMouseEntered(e -> 
        findPathsButton.setStyle(findPathsButton.getStyle() + 
            "-fx-background-color: #1E88E5;")); // Darker blue on hover
        findPathsButton.setOnMouseExited(e -> 
        findPathsButton.setStyle(findPathsButton.getStyle().replace(
            "#1E88E5", "#2196F3")));
       
        findPathsButton.setOnAction(e -> showPathOptions());
        
        

        Button criticalCorridorsBtn = createQuickButton("Critical Corridors", "#FF9800");
        criticalCorridorsBtn.setOnMouseEntered(e -> 
        findPathsButton.setStyle(criticalCorridorsBtn.getStyle() + 
            "-fx-background-color: #1E88E5;")); // Darker blue on hover
        criticalCorridorsBtn.setOnMouseExited(e -> 
        findPathsButton.setStyle(criticalCorridorsBtn.getStyle().replace(
            "#1E88E5", "#2196F3")));
      

        quickAccessBar.getChildren().addAll(addNodeBtn, findPathsButton, criticalCorridorsBtn);
        return quickAccessBar;
    }
    
    private void analyzeCriticalCorridors() {
        if (currentGraphView == null || currentMapViewer == null) {
            showStatus("Load a map first!");
            return;
        }

        // Clear any existing path highlights
        currentGraphView.clearHighlights();
        currentMapViewer.clearHighlights();

        if (corridorsHighlighted) {
            corridorsHighlighted = false;
            showStatus("Highlights cleared");
        } else {
            corridorAnalyzer.analyzeCriticalCorridors(
                Graph.getInstance(),
                currentGraphView,
                currentMapViewer
            );
            corridorsHighlighted = true;
            showStatus("Critical corridors highlighted");
        }
    }
    
    private void clearAllHighlights() {
        if(currentGraphView != null) currentGraphView.clearHighlights();
        if(currentMapViewer != null) {
        	startNode = null;
            endNode = null;
            currentMapViewer.clearMarkers();
            currentMapViewer.clearHighlights();
        } 
    }

    
    private void showPathOptions() {
        quickAccessBar.getChildren().clear();

        Button analyzePathsBtn = createQuickButton("Analyze Paths", "#2196F3");
        analyzePathsBtn.setOnAction(e -> enablePathAnalysisMode());

        Button poacherPathsBtn = createQuickButton("Poacher Paths", "#FF5722");
        poacherPathsBtn.setOnAction(e -> {
          
            clearAllHighlights();
            highlightPoacherPaths();
        });
		
        Button backBtn = createQuickButton("Back", "#9E9E9E");
        backBtn.setOnAction(e -> resetToDefaultQuickAccess());

        quickAccessBar.getChildren().addAll(analyzePathsBtn, poacherPathsBtn, backBtn);
    }
    

    private Button createQuickButton(String text, String baseColorHex) {
        Button btn = new Button(text);
        
        // Color adjustments for different states
        Color baseColor = Color.web(baseColorHex);
        Color hoverColor = baseColor.deriveColor(0, 1, 1.15, 1); // 15% lighter
        Color pressedColor = baseColor.deriveColor(0, 1, 0.85, 1); // 15% darker
        
        // Convert to hex strings
        String hoverHex = toHexString(hoverColor);
        String pressedHex = toHexString(pressedColor);
        
        // Base style
        String baseStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 12 24;", 
            baseColorHex);
        
        btn.setStyle(baseStyle);
        
        // Hover effects
        btn.setOnMouseEntered(e -> {
            String hoverStyle = baseStyle.replace(baseColorHex, hoverHex);
            btn.setStyle(hoverStyle);
        });
        
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        
        // Pressed effects
        btn.setOnMousePressed(e -> {
            String pressedStyle = baseStyle.replace(baseColorHex, pressedHex);
            btn.setStyle(pressedStyle);
        });
        
        btn.setOnMouseReleased(e -> btn.setStyle(baseStyle));
        
        btn.setPrefSize(180, 50);
        return btn;
    }

    // Helper to convert Color to hex string
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
     
 // New method to reset the graph
    private void resetGraph() {
        System.out.println("ResetGraph() called");  // Debug statement
        Graph.getInstance().resetToInitialState();
        
        if (currentGraphView != null) {
            System.out.println("Updating graph view");
            currentGraphView.update();
        }
        
        if (currentMapViewer != null) {
            System.out.println("Clearing map highlights");
            currentMapViewer.clearHighlights();
        }
        
        showStatus("Graph fully reset to original state.");
        System.out.println("Reset complete status shown");
    }

    
 // Modify switchToNoteMode method
    private void switchToNoteMode() {
    	
    	corridorsHighlighted = false;
        if (currentGraphView != null && currentMapViewer != null) {
            currentGraphView.clearHighlights();
            currentMapViewer.clearHighlights();
        }
        
        quickAccessBar.getChildren().clear();

        Button addGrass = createQuickButton("Back", "#8BC34A");
        Button addWater = createQuickButton("Add Water", "#03A9F4");
        Button addTrees = createQuickButton("Add Trees", "#4CAF50");
        Button addLand = createQuickButton("Add Land", "#795548");
        Button remove = createQuickButton("Remove", "#F44336");
        Button reset = createQuickButton("Reset", "#9E9E9E");
        reset.setOnAction(e -> {
            resetGraph();
            //resetToDefaultQuickAccess();
        });

        // Set up button actions
        addWater.setOnAction(e -> {
            currentNodeType = "water";
            setupNodeConversionHandler();
            showStatus("Water mode: Click nodes to convert");
        });
        
        addTrees.setOnAction(e -> {
            currentNodeType = "forest";
            setupNodeConversionHandler();
            showStatus("Forest mode: Click nodes to convert");
        });
        
        addLand.setOnAction(e -> {
            currentNodeType = "land";
            setupNodeConversionHandler();
            showStatus("Land mode: Click nodes to convert");
        });
        
        addGrass.setOnAction(e -> {
            currentNodeType = "grass";
            setupNodeConversionHandler();
            showStatus("Grass mode: Click nodes to convert");
        });
        
        addGrass.setOnAction(e -> resetToDefaultQuickAccess()); 

        remove.setOnAction(e -> {
            currentNodeType = "remove";
            setupNodeConversionHandler();
            showStatus("Removal mode: Click nodes to delete");
        });

        reset.setOnAction(e -> resetGraph());

        quickAccessBar.getChildren().addAll(addGrass, addWater, addTrees, addLand, remove, reset);
    }
    
    
    private void setupNodeConversionHandler() {
        if (currentGraphView != null) {
            currentGraphView.enableNodeSelection(selectedNode -> {
                if (currentNodeType.equals("remove")) {
                    removeNodeAndConnections(selectedNode);
                    currentGraphView.update(); // Full update needed for removal
                } else if (selectedNode instanceof ImageNode) {
                    ImageNode imageNode = (ImageNode) selectedNode;
                    String oldType = imageNode.getType();
                    imageNode.setType(currentNodeType);
                    
                    // Determine if edges were modified (water-related changes)
                    boolean wasWater = oldType.equalsIgnoreCase("water");
                    boolean isNowWater = currentNodeType.equalsIgnoreCase("water");
                    
                    if (wasWater || isNowWater) {
                        if (wasWater) {
                            connectToNearbyNodes(imageNode);
                        }
                        if (isNowWater) {
                            removeWaterConnections(imageNode);
                        }
                        currentGraphView.update(); // Full update for edge changes
                    } else {
                        currentGraphView.updateNodeAppearance(imageNode); // Partial update
                    }
                }
            });
        }
    }
    
    private void removeNodeAndConnections(Node node) {
        Graph graph = Graph.getInstance();
        graph.removeNode(node);
    }


    private void removeWaterConnections(Node node) {
        Graph graph = Graph.getInstance();
        graph.getEdges().removeIf(edge -> 
            edge.getFrom().equals(node) || edge.getTo().equals(node)
        );
    }

    private void connectToNearbyNodes(Node node) {
        Graph graph = Graph.getInstance();
        for (Node other : graph.getNodes()) {
            if (!other.equals(node) && !isWaterNode(other)) {
                double dx = node.getX() - other.getX();
                double dy = node.getY() - other.getY();
                double distance = Math.sqrt(dx*dx + dy*dy);
                
                if (distance <= CONNECTION_DISTANCE) {
                    graph.addEdge(new Edge(node, other));
                    graph.addEdge(new Edge(other, node));
                }
            }
        }
    }

    private boolean isWaterNode(Node node) {
        return (node instanceof ImageNode) && 
              ((ImageNode) node).getType().equalsIgnoreCase("water");
    }

    
    private void resetToDefaultQuickAccess() {
    	//resetGraph();
        currentNodeType = null;
        corridorsHighlighted = false; // Reset corridor state
        quickAccessBar.getChildren().clear();

        Button addNodeBtn = createQuickButton("Add Node", "#4CAF50");
        addNodeBtn.setOnAction(e -> switchToNoteMode());

        Button findPathsButton = createQuickButton("Find Paths", "#2196F3");
        findPathsButton.setOnAction(e -> showPathOptions());

        Button criticalCorridorsBtn = createQuickButton("Critical Corridors", "#FF9800");
        criticalCorridorsBtn.setOnAction(e -> analyzeCriticalCorridors());

        quickAccessBar.getChildren().addAll(addNodeBtn, findPathsButton, criticalCorridorsBtn);
        showStatus("Ready");
    }
    
    // When analyze paths is clicked, enable path selection and display instructional animations.
    private void enablePathAnalysisMode() {
        MapViewer currentViewer = getCurrentMapViewer();
        GraphView currentGraphView = getCurrentGraphView();
        
        if (currentViewer != null && currentGraphView != null) {
            // Clear previous highlights when starting new path analysis
            clearAllHighlights();
            currentViewer.showInstruction("Select start point on map or graph", 3000);
            
            // Unified selection handler for both views
            Consumer<Node> selectionHandler = selectedNode -> {
                if (startNode == null) {
                    // Selecting start node
                    currentMapViewer.clearMarkers();
                    currentGraphView.clearNodeHighlights();
                    startNode = selectedNode;
                    currentMapViewer.highlightStartNode(startNode);
                    currentGraphView.highlightStartNode(startNode);
                    currentViewer.showInstruction("Select end point", 3000);
                } else {
                    // Selecting end node
                    endNode = selectedNode;
                    currentMapViewer.highlightEndNode(endNode);
                    currentGraphView.highlightEndNode(endNode);
                    executeDijkstra();
                }
            };

            currentViewer.enablePathSelection(selectionHandler);
            currentGraphView.enableNodeSelection(selectionHandler);
        }
    }


    private void executeDijkstra() {
        if (startNode != null && endNode != null) {
            corridorsHighlighted = false;
            PathFinder pathFinder = new PathFinder();
            List<Node> path = pathFinder.findShortestPath(startNode, endNode);
            
            // Clear previous highlights before showing new path
            clearAllHighlights();
            
            if (!path.isEmpty()) {
                getCurrentGraphView().highlightPath(path);
                getCurrentMapViewer().highlightPath(path);
            } else {
                showStatus("No valid path exists! Path blocked by water.");
            }
        }
    }
    
    private GridPane createMetricsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(30);
        grid.setPadding(new Insets(40));
        grid.setAlignment(Pos.CENTER);

        VBox habitatBox = createMetricBox("habitat.png", "78%", "Habitat Connectivity", "#2E8B57");
        VBox riskBox = createMetricBox("risk.png", "3", "High-Risk Zones", "#FF9800");
        VBox sightingsBox = createMetricBox("sightings.png", "12", "Recent Sightings", "#2196F3");

        ListView<String> sightingsList = new ListView<>();
        sightingsList.getItems().addAll(
            "Elephant - Waterhole A (15 mins ago)",
            "Lion Pride - Northern Ridge (2 hrs ago)",
            "Rhino - Eastern Corridor (3 days ago)"
        );
        sightingsList.getStyleClass().add("sightings-list");
        sightingsList.setPrefSize(400, 200);

        grid.add(habitatBox, 0, 0);
        grid.add(riskBox, 1, 0);
        grid.add(sightingsBox, 2, 0);
        grid.add(sightingsList, 0, 1, 3, 1);

        return grid;
    }

    private VBox createMetricBox(String iconName, String value, String label, String color) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("metric-box");
        box.setMinSize(200, 150);

        try {
            ImageView icon = new ImageView(new Image(
                getClass().getResourceAsStream("/icons/" + iconName)));
            icon.setFitWidth(64);
            icon.setPreserveRatio(true);
            box.getChildren().add(icon);
        } catch (Exception e) {
            System.err.println("Missing metric icon: " + iconName);
        }

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 24px;");
        valueLabel.getStyleClass().add("metric-value");

        Label descLabel = new Label(label);
        descLabel.getStyleClass().add("metric-label");

        box.getChildren().addAll(valueLabel, descLabel);
        return box;
    }
    
    private void highlightPoacherPaths() {
        clearAllHighlights();

        Node criticalForest = findMostDenseForestNode();
        if (criticalForest == null || isEdgeNode(criticalForest)) {
            showStatus("No valid central forest node found!");
            return;
        }

        List<Node> edgeNodes = findEdgeNodes();
        System.out.println("Selected " + edgeNodes.size() + " representative edge nodes");

        // Highlight critical node
        currentGraphView.highlightCriticalNode(criticalForest);
        System.out.println("Critical forest at: " + criticalForest.getX() + "," + criticalForest.getY());

        // Find and sort paths by length
        List<PathResult> allPaths = new ArrayList<>();
        PathFinder finder = new PathFinder();
        
        for (Node edgeNode : edgeNodes) {
            List<Node> path = finder.findShortestPath(edgeNode, criticalForest);
            if (!path.isEmpty() && path.size() > 2) { // Filter trivial paths
                allPaths.add(new PathResult(path, calculatePathLength(path)));
            }
        }

        // Select top 10 shortest paths
        allPaths.sort(Comparator.comparingDouble(p -> p.length));
        List<List<Node>> topPaths = allPaths.stream()
            .limit(10)
            .map(p -> p.path)
            .collect(Collectors.toList());

        // Highlight selected paths
        topPaths.forEach(path -> {
            currentGraphView.appendPathHighlight(path);
            currentMapViewer.appendPathHighlight(path);
        });

        showStatus("Showing " + topPaths.size() + " most critical paths");
    }

    private double calculatePathLength(List<Node> path) {
        double length = 0;
        for (int i = 0; i < path.size()-1; i++) {
            Node a = path.get(i);
            Node b = path.get(i+1);
            length += Math.hypot(a.getX()-b.getX(), a.getY()-b.getY());
        }
        return length;
    }

    private static class PathResult {
        List<Node> path;
        double length;
        
        PathResult(List<Node> path, double length) {
            this.path = path;
            this.length = length;
        }
    }

    private Node findMostDenseForestNode() {
        double centerX = currentMapViewer.getImageWidth() / 2;
        double centerY = currentMapViewer.getImageHeight() / 2;
        
        return Graph.getInstance().getNodes().stream()
            .filter(n -> n instanceof ImageNode)
            .filter(n -> ((ImageNode) n).getType().equals("forest"))
            .filter(n -> !isEdgeNode(n))
            .min(Comparator.comparingDouble(n -> 
                Math.hypot(n.getX()-centerX, n.getY()-centerY))) // Prefer central nodes
            .orElse(null);
    }

    private boolean isEdgeNode(Node node) {
        List<Node> edgeNodes = findEdgeNodes();
        return edgeNodes.contains(node);
    }

    

    private List<Node> findEdgeNodes() {
        double imageWidth = currentMapViewer.getImageWidth();
        double imageHeight = currentMapViewer.getImageHeight();
        
        // More strict threshold (1% of image dimensions)
        double xThreshold = imageWidth * 0.01;
        double yThreshold = imageHeight * 0.01;
        
        List<Node> allEdges = Graph.getInstance().getNodes().stream()
            .filter(n -> n.getX() <= xThreshold || 
                        n.getX() >= imageWidth - xThreshold ||
                        n.getY() <= yThreshold || 
                        n.getY() >= imageHeight - yThreshold)
            .collect(Collectors.toList());

        // Select maximum 3 nodes per edge side
        return selectRepresentativeEdges(allEdges, imageWidth, imageHeight);
    }

    private List<Node> selectRepresentativeEdges(List<Node> edges, double width, double height) {
        List<Node> representatives = new ArrayList<>();
        double edgeThreshold = Math.min(width, height) * 0.02;

        Map<String, List<Node>> edgeGroups = new HashMap<>();
        edgeGroups.put("left", new ArrayList<>());
        edgeGroups.put("right", new ArrayList<>());
        edgeGroups.put("top", new ArrayList<>());
        edgeGroups.put("bottom", new ArrayList<>());

        for (Node n : edges) {
            if (n.getX() <= edgeThreshold) edgeGroups.get("left").add(n);
            else if (n.getX() >= width - edgeThreshold) edgeGroups.get("right").add(n);
            else if (n.getY() <= edgeThreshold) edgeGroups.get("top").add(n);
            else if (n.getY() >= height - edgeThreshold) edgeGroups.get("bottom").add(n);
        }

        // Select max 3 nodes per side, spaced at least 5% apart
        edgeGroups.forEach((side, nodes) -> {
            if (!nodes.isEmpty()) {
                nodes.sort(Comparator.comparingDouble(side.matches("left|right") ? 
                    Node::getY : Node::getX));
                
                double lastPos = -1;
                double spacing = side.matches("left|right") ? height * 0.05 : width * 0.05;
                
                for (Node n : nodes) {
                    double currentPos = side.matches("left|right") ? n.getY() : n.getX();
                    if (lastPos == -1 || Math.abs(currentPos - lastPos) >= spacing) {
                        representatives.add(n);
                        lastPos = currentPos;
                        if (representatives.size() >= 12) break; // 3 per side * 4 sides
                    }
                }
            }
        });

        return representatives.stream().distinct().collect(Collectors.toList());
    }

    
}
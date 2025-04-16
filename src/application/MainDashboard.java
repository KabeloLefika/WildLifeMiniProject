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
import java.util.List;
import java.util.function.Consumer;

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
        
        // Styling the background to be more dark
        setBackground(new Background(
            new BackgroundFill(Color.web("#121212"), CornerRadii.EMPTY, Insets.EMPTY)));
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
        
        // Initialize status label if necessary and position it to the right.
        if (statusLabel == null) {
            statusLabel = new Label("Ready");
        }
        BorderPane.setAlignment(statusLabel, Pos.CENTER_RIGHT);
        bottomPane.setRight(statusLabel);
        
        return bottomPane;
    }

    private VBox createSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(24);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(20));
        sidebar.setAlignment(Pos.TOP_CENTER);

        String[] menuItems = {"map_viewer", "data_management", "reports", "settings"};
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
        currentGraphView.getView().setPrefSize(500, 700); // Set preferred size for graph
        
        currentMapViewer = new MapViewer(graph, currentGraphView, () -> {
            globalProgress.setVisible(false);
            currentGraphView.update();
        });

        // Create titled containers with FIXED DIMENSIONS
        mapViewerContainer = new VBox(10);
        mapViewerContainer.setPrefSize(500, 700); // Fixed size for map container
        Label mapTitle = new Label("Original Map");
        mapTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        mapViewerContainer.getChildren().addAll(mapTitle, currentMapViewer.getView());
        mapViewerContainer.setAlignment(Pos.TOP_CENTER);

        graphViewContainer = new VBox(10);
        graphViewContainer.setPrefSize(500, 700); // Fixed size for graph container
        Label graphTitle = new Label("Network Graph");
        graphTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        graphViewContainer.getChildren().addAll(graphTitle, currentGraphView.getView());
        graphViewContainer.setAlignment(Pos.TOP_CENTER);

        // Container with FIXED layout dimensions
        mapGraphContainer = new HBox(20);
        mapGraphContainer.setPadding(new Insets(20));
        mapGraphContainer.setPrefSize(1040, 740); // 500+500 + 40 (spacing + padding)
        mapGraphContainer.setAlignment(Pos.CENTER);
        mapGraphContainer.getChildren().addAll(mapViewerContainer, graphViewContainer);

        Button toggleImageButton = new Button("Hide Original Image");
        toggleImageButton.setOnAction(e -> toggleOriginalImage(toggleImageButton));

        VBox mainContainer = new VBox(10);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().addAll(toggleImageButton, mapGraphContainer);
        
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
            statusLabel.getStyleClass().add("status-label");
        }
        statusLabel.setText(message);
    }

    // Clears the start and end nodes for path analysis.
    private void resetPathSelection() {
        startNode = null;
        endNode = null;
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
        addNodeBtn.setOnAction(e -> switchToNoteMode());

        Button findPathsButton = createQuickButton("Find Paths", "#2196F3");
        findPathsButton.setOnAction(e -> showPathOptions());

        Button criticalCorridorsBtn = createQuickButton("Critical Corridors", "#FF9800");
        criticalCorridorsBtn.setOnAction(e -> analyzeCriticalCorridors());

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

    
    private void showPathOptions() {
        quickAccessBar.getChildren().clear();

        Button analyzePathsBtn = createQuickButton("Analyze Paths", "#2196F3");
        analyzePathsBtn.setOnAction(e -> enablePathAnalysisMode());

        Button poacherPathsBtn = createQuickButton("Poacher Paths", "#FF5722");
        poacherPathsBtn.setOnAction(e -> {
        	//Adding poachers
            enablePoacherAnalysis();
            //Funding the path
            executePoacherDijkstra();
        });

        Button backBtn = createQuickButton("Back", "#9E9E9E");
        backBtn.setOnAction(e -> resetToDefaultQuickAccess());

        quickAccessBar.getChildren().addAll(analyzePathsBtn, poacherPathsBtn, backBtn);
    }

    


    private Button createQuickButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 5;");
        btn.getStyleClass().add("quick-action-button");
        btn.setPrefSize(180, 50);
        return btn;
    }
    
    private void enablePoacherAnalysis() {
    	PoacherAnalyzer pa = new PoacherAnalyzer();
        pa.enablePoacherAnalysis();
        
        // Update the graph view (and map viewer if needed) so new nodes are visible.
        if (currentGraphView != null) {
            currentGraphView.update();
        }
        
        showStatus("Poacher nodes added on map edges.");
        
    }

    private void executePoacherDijkstra() {
        Graph g = Graph.getInstance();
        List<Node> allPoacherNodes = new ArrayList<>();
        
        // Collect all poacher nodes.
        for (Node n : g.getNodes()) {
            if (n instanceof ImageNode && ((ImageNode) n).getType().equals("poacher")) {
                allPoacherNodes.add(n);
            }
        }
        
        if (allPoacherNodes.isEmpty()) {
            showStatus("No poacher node found. Please enable poacher analysis first.");
            return;
        }
        
        PoacherAnalyzer pa = new PoacherAnalyzer();
        boolean pathFound = false;
        
        // Loop through each poacher node and append its path highlight.
        for (Node poacherStart : allPoacherNodes) {
            List<Node> path = pa.executePoacherDijstra(poacherStart);
            if (path != null && !path.isEmpty()) {
                pathFound = true;
                if (currentGraphView != null) {
                    currentGraphView.appendPathHighlight(path);
                }
                if (currentMapViewer != null) {
                    currentMapViewer.appendPathHighlight(path);
                }
            } else {
                System.out.println("No path found for poacher at: (" + poacherStart.getX() + ", " + poacherStart.getY() + ")");
            }
        }
        
        if (pathFound) {
            showStatus("All poacher paths highlighted.");
        } else {
            showStatus("No forest reachable from any poacher node.");
        }
    }


    
 // Modify switchToNoteMode method
    private void switchToNoteMode() {
    	
    	corridorsHighlighted = false;
        if (currentGraphView != null && currentMapViewer != null) {
            currentGraphView.clearHighlights();
            currentMapViewer.clearHighlights();
        }
        
        quickAccessBar.getChildren().clear();

        Button addGrass = createQuickButton("Add Grass", "#8BC34A");
        Button addWater = createQuickButton("Add Water", "#03A9F4");
        Button addTrees = createQuickButton("Add Trees", "#4CAF50");
        Button addLand = createQuickButton("Add Land", "#795548");
        Button remove = createQuickButton("Remove", "#F44336");
        Button reset = createQuickButton("Reset", "#9E9E9E");

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

        remove.setOnAction(e -> {
            currentNodeType = "remove";
            setupNodeConversionHandler();
            showStatus("Removal mode: Click nodes to delete");
        });

        reset.setOnAction(e -> resetToDefaultQuickAccess());

        quickAccessBar.getChildren().addAll(addGrass, addWater, addTrees, addLand, remove, reset);
    }
    
    private void setupNodeConversionHandler() {
        if (currentGraphView != null) {
            currentGraphView.enableNodeSelection(selectedNode -> {
                if (currentNodeType.equals("remove")) {
                    // Handle node removal
                    Graph.getInstance().removeNode(selectedNode);
                    currentGraphView.update();
                    currentMapViewer.getView(); // Refresh map view if needed
                } else if (selectedNode instanceof ImageNode) {
                    // Convert node type
                    ImageNode imageNode = (ImageNode) selectedNode;
                    imageNode.setType(currentNodeType);
                    currentGraphView.update();
                }
            });
        }
    }

    
    private void resetToDefaultQuickAccess() {
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
            currentViewer.showInstruction("Select start point on map or graph", 3000);
            
            // Unified selection handler for both views
            Consumer<Node> selectionHandler = selectedNode -> {
                if (startNode == null) {
                    startNode = selectedNode;
                    currentViewer.showInstruction("Select end point", 3000);
                } else {
                    endNode = selectedNode;
                    executeDijkstra();
                    resetPathSelection();
                }
            };

            currentViewer.enablePathSelection(selectionHandler);
            currentGraphView.enableNodeSelection(selectionHandler);
        }
    }

    private void executeDijkstra() {
        if (startNode != null && endNode != null) {
            // Clear existing corridor highlights
            corridorsHighlighted = false;
            
            PathFinder pathFinder = new PathFinder();
            List<Node> path = pathFinder.findShortestPath(startNode, endNode);
            
            if (!path.isEmpty()) {
                getCurrentGraphView().highlightPath(path);
                getCurrentMapViewer().highlightPath(path);
            } else {
                showStatus("No path found!");
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
}
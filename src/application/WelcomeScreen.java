// WelcomeScreen.java
package application;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;

public class WelcomeScreen extends StackPane {
    private final Stage primaryStage;
    private final Image[] slideshowImages;
    private int currentImageIndex = 0;

    public WelcomeScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.slideshowImages = loadSlideshowImages();
        initializeUI();
        startSlideshow();
    }

    private void initializeUI() {
        // Create semi-transparent overlay
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.color(0, 0, 0, 0.4));
        overlay.widthProperty().bind(widthProperty());
        overlay.heightProperty().bind(heightProperty());

        // Logo and Title Container
        VBox logoContainer = new VBox(30);
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPadding(new Insets(60));

        ImageView logo = new ImageView(loadImage("/images/logo.jpg"));
        logo.setFitWidth(300);
        logo.setPreserveRatio(true);

        Label titleLabel = new Label("Wild Graph");
        titleLabel.getStyleClass().add("title-label");

        logoContainer.getChildren().addAll(titleLabel);

        // Button Container
        HBox buttonBox = new HBox(30);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(40));

        Button loginButton = createButton("Login", "#4CAF50", "#45a049");
        loginButton.setOnAction(e -> showLoginScreen());
        //{
            // Create dashboard instance
           // MainDashboard dashboard = new MainDashboard(primaryStage);
            
            // Create new scene with dashboard
            //Scene dashboardScene = new Scene(dashboard, 1280, 720);
            //dashboardScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            
            // Switch scenes
            //primaryStage.setScene(dashboardScene);
            //primaryStage.setTitle("Wild Graph - Dashboard");
        //});
        Button helpButton = createButton("Help", "#2196F3", "#1976D2");

        buttonBox.getChildren().addAll(loginButton, helpButton);

        // Main Content Pane
        VBox mainContent = new VBox(60);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.getChildren().addAll(logoContainer, buttonBox);

        // Assemble layers
        getChildren().addAll(overlay, mainContent);
    }

    private Button createButton(String text, String defaultColor, String hoverColor) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setStyle("-fx-background-color: " + defaultColor + ";");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + ";"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + defaultColor + ";"));
        return button;
    }

    private void startSlideshow() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> {
                currentImageIndex = (currentImageIndex + 1) % slideshowImages.length;
                setBackground(new Background(
                    new BackgroundImage(slideshowImages[currentImageIndex],
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                    )
                ));
            })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception ex) {
            System.err.println("Error loading image: " + path);
            ex.printStackTrace();
            return null;
        }
    }

    private Image[] loadSlideshowImages() {
        return new Image[] {
            loadImage("/images/reserver1.jpg"),
            loadImage("/images/reserver2.jpg"),
            loadImage("/images/reserver3.jpg")
        };
    }

    private void showLoginScreen() {
    	LoginScreen loginScreen = new LoginScreen(primaryStage);
        Scene loginScene = new Scene(loginScreen, 600, 400);
        //System.out.println("Login button clicked");
        // Implement login functionality here
    	loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Wild Graph - Login"); 
    }
    
    private void showHelpDialog() {
        Stage helpStage = new Stage();
        helpStage.initModality(Modality.APPLICATION_MODAL);
        helpStage.setTitle("Wild Graph Help");

        Label helpText = new Label("""
            Welcome to Wild Graph!
            
            • Click 'Login' to access advanced features
            • Use 'Help' to view this guide anytime
            • Explore wildlife data visualization tools
            """);
        helpText.getStyleClass().add("help-text");
        helpText.setWrapText(true);
        helpText.setMaxWidth(600);
        helpText.setPadding(new Insets(40));

        Scene helpScene = new Scene(helpText, 800, 600);
        helpScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        helpStage.setScene(helpScene);
        helpStage.show();
    }
}
package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        WelcomeScreen welcomeScreen = new WelcomeScreen(primaryStage);
        
        // Create a Scene with the WelcomeScreen as root node
        Scene scene = new Scene(welcomeScreen, 800, 600); // Set initial size
        
        // Add stylesheet
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setTitle("Wild Graph");
        primaryStage.setScene(scene); // Now passing a Scene object
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
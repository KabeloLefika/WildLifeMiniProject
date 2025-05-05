package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

//file reading
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginScreen extends VBox{
	private final Stage primaryStage;
	
	public LoginScreen(Stage primaryStage) {
		this.primaryStage = primaryStage;
		intializeUI();
	}
	private void intializeUI() {
		setSpacing(15);
		setPadding(new Insets(40));
		setAlignment(Pos.CENTER);
		getStyleClass().add("login-root");
		
		Label titleLabel= new Label("Login");
		titleLabel.getStyleClass().add("title-label");
		//titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24px;");
		
		Label emailLabel = new Label("Enter your email:");
        emailLabel.setPrefWidth(300);
        emailLabel.setAlignment(Pos.CENTER_LEFT);
        emailLabel.getStyleClass().add("form-label");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMaxWidth(300);
        emailField.getStyleClass().add("text-field");

        // Password label and field
        Label passwordLabel = new Label("Enter your password:");
        passwordLabel.setPrefWidth(300);
        passwordLabel.setAlignment(Pos.CENTER_LEFT);
        passwordLabel.getStyleClass().add("form-label");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(300);
        passwordField.getStyleClass().add("text-field");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));
        loginButton.getStyleClass().add("login-button");
        
        Label registerLabel = new Label("Not registered? ");
        registerLabel.getStyleClass().add("form-label");
        //Redirect link to the register  page
        Hyperlink registerLink = new Hyperlink("Register");
        registerLink.setOnAction(e -> openRegisterScreen());
        registerLink.getStyleClass().add("register-link");

        HBox registerBox = new HBox(registerLabel, registerLink);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setSpacing(5);

        getChildren().addAll(titleLabel, emailLabel, emailField, passwordLabel, passwordField, loginButton, registerBox);

	}
	    private void handleLogin(String email, String password) {
	        if(email.isEmpty() || password.isEmpty()) {
	        	showAlert("Please enter email and password.");
	        	return;
	        }
	    	
	        try(BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
	        	String line;
	        	while((line= reader.readLine())!= null){
	        		String[] userDetails = line.split(",");
	        		if(userDetails.length>= 4 && userDetails[2].equals(email) && userDetails[3].equals(password)) {
	        			showAlert("Login successful! Redirecting to dashboard.");
	        			openMainDashboard();
	        			return;
	        		}
	        	}
	        	showAlert("Invalid email or password");
	        }
	        catch(IOException e) {
	        	showAlert("Problem reading user data! Please try again");
	        }
	    }

	    private void openRegisterScreen() {
	    	Scene registerScene = new Scene(new RegisterScreen(primaryStage), 600, 500);
	        registerScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
	        primaryStage.setScene(registerScene);
	        primaryStage.setTitle("Wild Graph - Register");
	    }
	    
	    ////Success login going to home page
	    private void openMainDashboard() {
	    	MainDashboard dashboard = new MainDashboard(primaryStage);
	        Scene scene = new Scene(dashboard, 1200, 800); 
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Main Dashboard");
	        primaryStage.show();
	    }

	    ////Alert method
	    private void showAlert(String message) {
	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Notification");
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    }
}

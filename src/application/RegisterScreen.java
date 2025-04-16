package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

//File writing
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class RegisterScreen extends VBox {
    private final Stage primaryStage;

    public RegisterScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeUI();
    }

    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(40));
        setAlignment(Pos.CENTER);
        getStyleClass().add("register-root");

        Label titleLabel = new Label("Register");
        titleLabel.getStyleClass().add("title-label");
        //titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24px;");

        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton userRadio = new RadioButton("User");
        userRadio.setToggleGroup(roleGroup);
        userRadio.setSelected(true);
        userRadio.getStyleClass().add("radio-button");
        
        RadioButton adminRadio = new RadioButton("Admin");
        adminRadio.setToggleGroup(roleGroup);
        adminRadio.getStyleClass().add("radio-button");
        
        //Name Field
        Label nameLabel = new Label("Enter your name:");
        nameLabel.getStyleClass().add("form-label");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setMaxWidth(300);
        nameField.getStyleClass().add("text-field");

        //Suename Field
        Label surnameLabel = new Label("Enter your surname:");
        surnameLabel.getStyleClass().add("form-label");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Enter your surname");
        surnameField.setMaxWidth(300);
        surnameField.getStyleClass().add("text-field");

        // Email Field
        Label emailLabel = new Label("Enter your email:");
        emailLabel.getStyleClass().add("form-label");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMaxWidth(300);
        emailField.getStyleClass().add("text-field");

        //Label Field
        Label genderLabel = new Label("Select Gender:");
        genderLabel.getStyleClass().add("form-label");
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Other");
        genderCombo.setPromptText("Select Gender");
        genderCombo.setMaxWidth(300);
        genderCombo.getStyleClass().add("combo-box");

        // Password Fiels
        Label passwordLabel = new Label("Enter your password:");
        passwordLabel.getStyleClass().add("form-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(300);
        passwordField.getStyleClass().add("text-field");

        // Confirm Password Field
        Label confirmPasswordLabel = new Label("Confirm your password:");
        confirmPasswordLabel.getStyleClass().add("form-label");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setMaxWidth(300);
        confirmPasswordField.getStyleClass().add("text-field");

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("register-button");
        registerButton.setOnAction(e -> handleRegister(
            userRadio.isSelected() ? "User" : "Admin",
            nameField.getText(),
            surnameField.getText(),
            emailField.getText(),
            genderCombo.getValue(),
            passwordField.getText(),
            confirmPasswordField.getText()));
        
        Label loginLabel = new Label("Already have an account? ");
        loginLabel.getStyleClass().add("form-label");
        Hyperlink loginLink = new Hyperlink("Login");
        loginLink.getStyleClass().add("login-link");
        loginLink.setOnAction(e -> openLoginScreen());

        HBox loginBox = new HBox(loginLabel, loginLink);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setSpacing(5);

        getChildren().addAll(titleLabel,
                userRadio, adminRadio,
                nameLabel, nameField,
                surnameLabel, surnameField,
                emailLabel, emailField,
                genderLabel, genderCombo,
                passwordLabel, passwordField,
                confirmPasswordLabel, confirmPasswordField,
                registerButton,
                loginBox);
    }

    //Registration function
    private void handleRegister(String role, String name, String surname, String email,String gender, String password, String confirmPassword) {
    	
    	if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
    		showAlert("All fields are required!!");
    		return;
    	}
    	
    	if (!password.equals(confirmPassword)) {
    		showAlert("Passwords do not match!");
    		return;
    	}
    	
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt",true))){
    		writer.write(name + "," + surname + "," + email + "," + password + "," + role);
    		writer.newLine();
    		showAlert("Registration Successful! Redirecting to login");
    		openLoginScreen();
    	}
    	catch(IOException e) {
    		showAlert("Problem while saving data! Please try again");
    	}
    }

    private void openLoginScreen() {
    	LoginScreen loginScreen = new LoginScreen(primaryStage);
        Scene loginScene = new Scene(loginScreen, 600, 400);
    	loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Wild Graph - Login"); 
    }
    
    ////Alert method
    private void showAlert(String Message) {
    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	alert.setTitle("Notification");
    	alert.setHeaderText(null);
    	alert.setContentText(Message);
    	alert.showAndWait();
    }
}

package com.example.application.Controller;

import com.example.application.Database.DBC;
import javafx.application.Platform;
import com.example.application.Models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Login implements Initializable {


    //First : User name and password
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label error_msg;
    @FXML private Label timerLabel;
    @FXML private Button loginButton;

    public static String loggedInUser;



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startTimerThread();
    }


    private void startTimerThread() {
        Thread timerThread = new Thread(() -> {
            int seconds = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                    final int currentSec = ++seconds;//final to enable to use it in Platform.runLater
                    Platform.runLater(() -> {//why run later? - because javafx doesn't allow another thread to run
                        if (timerLabel != null) {
                            timerLabel.setText("Seconds since start: " + currentSec);
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        timerThread.setDaemon(true);//ends when the whole project stopped
        timerThread.start();
    }

    //Second : Regex validaion
    private static final Pattern USER_PATTERN = Pattern.compile("^[A-Za-z]{3,}$");
    private static final Pattern PASS_PATTERN = Pattern.compile("^[A-Za-z]{3,}[0-9]{3,}$");


    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        //General request : input validation
        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Please fill all fields.");
            return;
        }

        if (!USER_PATTERN.matcher(username).matches()) {
            showErrorMessage("Username must be at least 3 letters (letters only).");
            return;
        }

        if (!PASS_PATTERN.matcher(password).matches()) {
            showErrorMessage("Password must start with at least 3 letters followed by at least 3 numbers.");
            return;
        }

        String sql = "SELECT ROLE, PASSWORD, EMPLOYEE_ID FROM \"USER\" WHERE USER_NAME = ?";

        //Third : DataBase Authentication
        try (Connection connection = DBC.getInstance().getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql)) {

            pstm.setString(1, username);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) { //check if there is at least one output from the query execution
                String dbPassword = rs.getString("PASSWORD");
                String role = rs.getString("ROLE");

                //Fourth : Error Handling
                if (!dbPassword.equals(password)) {
                    showErrorMessage("Incorrect Password.");
                    return;
                }

                loggedInUser = username;

                //if ROLE is admin -> shows dashboard that designed for admin
                if (role.equalsIgnoreCase("admin")) {
                    openDashboard("/view/Admin/Dashboard.fxml", "Admin Dashboard");
                } else if (role.equalsIgnoreCase("employee")) {
                    User.setEmployeeId(rs.getString("EMPLOYEE_ID"));
                    //if ROLE is employee -> shows dashboard designed for employee
                    openDashboard("/view/Employee/DashboardEView.fxml", "Employee Dashboard");
                } else {
                    showErrorMessage("Unknown Role.");
                }

            } else {
                showErrorMessage("User not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Database Error: " + e.getMessage());
        }
    }

    private void showErrorMessage(String message) {
        if (error_msg != null) {
            error_msg.setText(message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    private void openDashboard(String fxmlPath, String title) throws Exception {
        URL location = getClass().getResource(fxmlPath);
        if (location == null) {
            System.err.println("Could not find FXML: " + fxmlPath);
            showErrorMessage("Dashboard FXML not found!");
            return;
        }

        FXMLLoader loader = new FXMLLoader(location);
        Parent parent = loader.load();
        Scene scene = new Scene(parent);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setMaximized(true);
        stage.show();
    }

    @FXML
    public void handleForgotPassword(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset Password");
        alert.setHeaderText("Support Contact");
        alert.setContentText("Please contact the Administrator to reset your password.\nEmail: admin@cafemoon.com");
        alert.showAndWait();
    }
}

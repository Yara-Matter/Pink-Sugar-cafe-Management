package com.example.application.Controller.Employee;

import com.example.application.Controller.BaseController;
import com.example.application.Models.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import java.io.IOException;

public class AboutEController extends BaseController {

    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() {
        super.initialize(null, null);

    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            ThemeManager.setScene(scene);

            stage.setScene(scene);
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML void handleDashboard(ActionEvent e) { switchScene(e, "/view/Employee/DashboardEView.fxml"); }
    @FXML void handleCustomer(ActionEvent e) { switchScene(e, "/view/Employee/CustomerView.fxml"); }
    @FXML void handleAbout(ActionEvent e) { switchScene(e, "/view/Employee/AboutEView.fxml"); }
    @FXML void handleSettings(ActionEvent e) { switchScene(e, "/view/Employee/SettingsEView.fxml"); }
    @FXML void handleMenu(ActionEvent e) { switchScene(e, "/view/Employee/MenuView.fxml"); }
    @FXML void handleLogout(ActionEvent e) { switchScene(e, "/view/login.fxml"); }
}

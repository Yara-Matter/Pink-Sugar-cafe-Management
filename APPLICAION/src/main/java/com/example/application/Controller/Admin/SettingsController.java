package com.example.application.Controller.Admin;

import com.example.application.Controller.BaseController;
import com.example.application.Database.DBC;
import com.example.application.Models.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

public class SettingsController extends BaseController {

    @FXML private Slider fontSlider;
    @FXML private Label fontValueLabel;
    @FXML private Label dbStatusLabel;
    @FXML private ToggleButton darkModeToggle;
    @FXML private ImageView logoImageView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);

        // Font Size
        double currentFontSize = ThemeManager.getCurrentFontSize();
        fontSlider.setValue(currentFontSize);
        updateFontLabel(currentFontSize);

        fontSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double size = newVal.doubleValue();
            updateFontLabel(size);
            ThemeManager.applyFontSize(size);
        });



        //Dark / Light Mode
        boolean isDark = ThemeManager.isDarkMode();

        darkModeToggle.setSelected(!isDark);
        darkModeToggle.setText(isDark ? "On" : "Off");

        updateLogoForTheme();

        darkModeToggle.selectedProperty().addListener((obs, oldVal, isSelected) -> {
            ThemeManager.switchTheme(!isSelected);
            updateLogoForTheme();
            darkModeToggle.setText(isSelected ? "On" : "Off");
        });
    }

    private void updateFontLabel(double size) {
        fontValueLabel.setText(String.format("%.0fpx", size));
    }

    // Logo Switch
    public void updateLogoForTheme() {
        if (logoImageView == null) return;

        String path = ThemeManager.isDarkMode()
                ? "/images/logo.png"
                : "/images/light.png";

        logoImageView.setImage(new Image(getClass().getResourceAsStream(path)));
    }

    // DB Test
    @FXML
    private void testDBConnection(ActionEvent event) {
        try (Connection conn = DBC.getInstance().getConnection()) {
            if (conn != null && !conn.isClosed()) {
                dbStatusLabel.setText("Status: Connection Successful!");
                dbStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            dbStatusLabel.setText("Status: Connection Failed!");
            dbStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    // Navigation
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
            ThemeManager.setScene(scene);
            stage.setScene(scene);
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML void handleDashboard(ActionEvent e) { switchScene(e, "/view/Admin/Dashboard.fxml"); }
    @FXML void handleSales(ActionEvent e) { switchScene(e, "/view/Admin/Sales.fxml"); }
    @FXML void handleInventory(ActionEvent e) { switchScene(e, "/view/Admin/Inventory.fxml"); }
    @FXML void handleAddEmployee(ActionEvent e) { switchScene(e, "/view/Admin/AddEmployee.fxml"); }
    @FXML void handleUpdateEmployee(ActionEvent e) { switchScene(e, "/view/Admin/UpdateEmployee.fxml"); }
    @FXML void handleAbout(ActionEvent e) { switchScene(e, "/view/Admin/About.fxml"); }
    @FXML void handleSettings(ActionEvent e) { switchScene(e, "/view/Admin/Settings.fxml"); }
    @FXML void handleLogout(ActionEvent e) { switchScene(e, "/view/login.fxml"); }
}

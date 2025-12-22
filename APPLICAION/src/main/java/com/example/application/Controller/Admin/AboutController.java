package com.example.application.Controller.Admin;

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

public class AboutController extends BaseController {

    @FXML
    private BorderPane rootPane; // root container من FXML

    @FXML
    public void initialize() {
        super.initialize(null, null); // ← لتحديث اللوجو حسب الثيم
        // حجم الخط يتم تطبيقه تلقائي عن طريق ThemeManager عند فتح الصفحة
    }

    // دالة لإعادة التوجيه بين الصفحات
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // تطبيق الثيم وحجم الخط تلقائي على الصفحة الجديدة
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

    // ==== أزرار الـ Sidebar ====
    @FXML void handleDashboard(ActionEvent event) { switchScene(event, "/view/Admin/Dashboard.fxml"); }
    @FXML void handleLogout(ActionEvent event) { switchScene(event, "/view/login.fxml"); }
    @FXML void handleSales(ActionEvent event) { switchScene(event, "/view/Admin/Sales.fxml"); }
    @FXML void handleInventory(ActionEvent event) { switchScene(event, "/view/Admin/Inventory.fxml"); }
    @FXML void handleAddEmployee(ActionEvent event) { switchScene(event, "/view/Admin/AddEmployee.fxml"); }
    @FXML void handleUpdateEmployee(ActionEvent event) { switchScene(event, "/view/Admin/UpdateEmployee.fxml"); }
    @FXML void handleAbout(ActionEvent event) { switchScene(event, "/view/Admin/About.fxml"); }
    @FXML void handleSettings(ActionEvent event) { switchScene(event, "/view/Admin/Settings.fxml"); }
}

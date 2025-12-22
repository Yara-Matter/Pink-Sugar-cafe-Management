package com.example.application.Controller.Admin;

import com.example.application.Controller.BaseController;
import com.example.application.Database.DBC;
import com.example.application.Models.ThemeManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;
    @FXML private Label timeLabel, dateLabel, salesLabel, ordersLabel, customersLabel, userNameLabel;
    @FXML private AreaChart<String, Number> inventoryChart;
    @FXML private PieChart salesPieChart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        updateLogoForTheme();

        startClock();

        if (com.example.application.Controller.Login.loggedInUser != null) {
            userNameLabel.setText(com.example.application.Controller.Login.loggedInUser);
        }

        loadDashboardStats();
        loadCharts();
    }

    public void updateLogoForTheme() {
        if (logoImageView == null) return;
        String path = ThemeManager.isDarkMode() ? "/images/logo.png" : "/images/light.png";
        try {
            logoImageView.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception e) { System.err.println("Logo missing: " + path); }
    }

    private void loadDashboardStats() {
        try (Connection con = DBC.getInstance().getConnection();
             Statement st = con.createStatement()) {

            ResultSet rsSales = st.executeQuery("SELECT SUM(TOTAL) FROM SALE");
            if (rsSales.next()) salesLabel.setText("$ " + String.format("%.2f", rsSales.getDouble(1)));

            ResultSet rsOrders = st.executeQuery("SELECT COUNT(*) FROM SALE");
            if (rsOrders.next()) ordersLabel.setText(String.valueOf(rsOrders.getInt(1)));

            ResultSet rsCust = st.executeQuery("SELECT COUNT(*) FROM CUSTOMER");
            if (rsCust.next()) customersLabel.setText(String.valueOf(rsCust.getInt(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCharts() {
        // AreaChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");
        String sql1 = "SELECT TO_CHAR(s.SALE_DATE, 'Mon') AS M, SUM(sd.QUANTITY) AS Q " +
                "FROM SALE s JOIN SALE_DETAIL sd ON s.SALE_ID = sd.SALE_ID " +
                "GROUP BY TO_CHAR(s.SALE_DATE, 'Mon'), TO_CHAR(s.SALE_DATE, 'MM') ORDER BY TO_CHAR(s.SALE_DATE, 'MM')";
        try (Connection con = DBC.getInstance().getConnection();
             ResultSet rs = con.createStatement().executeQuery(sql1)) {
            while (rs.next()) series.getData().add(new XYChart.Data<>(rs.getString("M"), rs.getInt("Q")));
            inventoryChart.getData().setAll(series);
        } catch (Exception e) { e.printStackTrace(); }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        String sql2 = "SELECT c.NAME, SUM(s.TOTAL) FROM SALE s JOIN CUSTOMER c ON s.CUS_ID = c.CUS_ID GROUP BY c.NAME";
        try (Connection con = DBC.getInstance().getConnection();
             ResultSet rs = con.createStatement().executeQuery(sql2)) {
            while (rs.next()) pieData.add(new PieChart.Data(rs.getString(1), rs.getDouble(2)));
            salesPieChart.setData(pieData);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startClock() {
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                    dateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
                });
            }
        }, 0, 1000);
    }

    private void switchScene(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight());
            ThemeManager.setScene(scene);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void handleDashboard(ActionEvent e){ switchScene(e,"/view/Admin/Dashboard.fxml"); }
    @FXML void handleSales(ActionEvent e){ switchScene(e,"/view/Admin/Sales.fxml"); }
    @FXML void handleInventory(ActionEvent e){ switchScene(e,"/view/Admin/Inventory.fxml"); }
    @FXML void handleAddEmployee(ActionEvent e){ switchScene(e,"/view/Admin/AddEmployee.fxml"); }
    @FXML void handleUpdateEmployee(ActionEvent e){ switchScene(e,"/view/Admin/UpdateEmployee.fxml"); }
    @FXML void handleAbout(ActionEvent e){ switchScene(e,"/view/Admin/About.fxml"); }
    @FXML void handleSettings(ActionEvent e){ switchScene(e,"/view/Admin/Settings.fxml"); }
    @FXML void handleLogout(ActionEvent e){ switchScene(e,"/view/login.fxml"); }
}
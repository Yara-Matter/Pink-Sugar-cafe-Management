package com.example.application.Controller.Admin;

import com.example.application.Database.DBC;
import com.example.application.Models.Sale;
import com.example.application.Models.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class SalesController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, String> colSaleId, colCustomerName, colContact, colDate;
    @FXML private TableColumn<Sale, Double> colTotal;
    @FXML private TextField searchField;

    private final ObservableList<Sale> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ThemeManager.setScene(newScene);


                Image img = ThemeManager.isDarkMode() ?
                        new Image(getClass().getResourceAsStream("/images/logo.png")) :
                        new Image(getClass().getResourceAsStream("/images/light.png"));
                logoImageView.setImage(img);

                setActiveButton("Sales");
            }
        });

        setupTableColumns();
        loadSalesData();
        setupSearch();
    }


    // TABLE METHODS
    private void setupTableColumns() {
        colSaleId.setCellValueFactory(cellData -> cellData.getValue().saleIdProperty());
        colCustomerName.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        colContact.setCellValueFactory(cellData -> cellData.getValue().contactProperty());
        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
    }

    private void loadSalesData() {
        masterData.clear();
        String sql = "SELECT s.SALE_ID, c.NAME, c.CONTACT_NUMBER AS PHONE, s.TOTAL, TO_CHAR(s.SALE_DATE,'YYYY-MM-DD') as S_DATE " +
                "FROM SALE s JOIN CUSTOMER c ON s.CUS_ID = c.CUS_ID";
        try (Connection conn = DBC.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                masterData.add(new Sale(
                        rs.getString("SALE_ID"),
                        rs.getString("NAME"),
                        rs.getString("PHONE"),
                        rs.getDouble("TOTAL"),
                        rs.getString("S_DATE")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        salesTable.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<Sale> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(sale -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return sale.getSaleId().toLowerCase().contains(filter) ||
                        sale.getCustomerName().toLowerCase().contains(filter) ||
                        sale.getContact().contains(filter);
            });
            salesTable.setItems(filteredData);
        });
    }


    // NAVIGATION
    private void switchScene(ActionEvent e, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            ThemeManager.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void setActiveButton(String active) {
        rootPane.lookupAll(".sidebar-button").forEach(node -> {
            node.getStyleClass().remove("active");
            if (node instanceof Button btn && btn.getText().equals(active)) {
                btn.getStyleClass().add("active");
            }
        });
    }

    @FXML void handleDashboard(ActionEvent e){ switchScene(e,"/view/Admin/Dashboard.fxml"); }
    @FXML void handleSales(ActionEvent e){ switchScene(e,"/view/Admin/Sales.fxml"); }
    @FXML void handleInventory(ActionEvent e){ switchScene(e,"/view/Admin/Inventory.fxml"); }
    @FXML void handleAddEmployee(ActionEvent e){ switchScene(e,"/view/Admin/AddEmployee.fxml"); }
    @FXML void handleUpdateEmployee(ActionEvent e){ switchScene(e,"/view/Admin/UpdateEmployee.fxml"); }
    @FXML void handleAbout(ActionEvent e){ switchScene(e,"/view/Admin/About.fxml"); }
    @FXML void handleSettings(ActionEvent e){ switchScene(e,"/view/Admin/Settings.fxml"); }
    @FXML void handleLogout(ActionEvent e){ switchScene(e,"/view/login.fxml"); }



    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

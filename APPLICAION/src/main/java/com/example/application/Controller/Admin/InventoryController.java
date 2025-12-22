package com.example.application.Controller.Admin;

import com.example.application.Controller.BaseController;
import com.example.application.Database.DBC;
import com.example.application.Models.Product;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class InventoryController extends BaseController {

    // Fields
    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;
    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, String> colId, colName, colType;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TextField idField, nameField, stockField, priceField, searchField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ImageView productImageView;

    // Data
    private final ObservableList<Product> masterData = FXCollections.observableArrayList();
    private String selectedImagePath = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        updateLogoForTheme();


        // Table Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        //ComboBox
        typeComboBox.getItems().addAll("Snacks", "Coffee", "Juice", "Desserts");
        //Load Data
        loadInventoryData();
        //Setup Search
        setupSearch();
    }


    // DATABASE METHODS
    private void loadInventoryData() {
        masterData.clear();
        String sql = "SELECT PRODUCT_ID, PRODUCT_NAME, PRODUCT_TYPE, STOCK, PRICE, IMAGE FROM PRODUCT ORDER BY PRODUCT_ID";
        try (Connection conn = DBC.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                masterData.add(new Product(
                        rs.getString("PRODUCT_ID"),
                        rs.getString("PRODUCT_NAME"),
                        rs.getString("PRODUCT_TYPE"),
                        rs.getInt("STOCK"),
                        rs.getDouble("PRICE"),
                        rs.getString("IMAGE")
                ));
            }
            inventoryTable.setItems(masterData);

        } catch (SQLException e) {
            showAlert("DB Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }




    @FXML
    private void handleAdd(ActionEvent event) {
        if (idField.getText().isEmpty() || nameField.getText().isEmpty() ||
                typeComboBox.getValue() == null || stockField.getText().isEmpty() || priceField.getText().isEmpty()) {
            showAlert("Error", "All fields are required", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = DBC.getInstance().getConnection()) {

            String pathForDB = "";

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                // Find the index of the last backslash (the one before the filename)
                int lastBackslash = selectedImagePath.lastIndexOf('\\');

                if (lastBackslash != -1) {
                    // Find the index of the backslash before that (the one before "MenuImages")
                    //We search backwards starting from the position just before the last backslash
                    int secondToLastBackslash = selectedImagePath.lastIndexOf('\\', lastBackslash - 1);
                    if (secondToLastBackslash != -1) {
                        // Extract the substring from the second backslash to the end
                        // This will give you "\MenuImages\IceCoffee.jpg"
                        pathForDB = selectedImagePath.substring(secondToLastBackslash);
                    } else {
                        // Fallback if only one backslash is found
                        pathForDB = selectedImagePath.substring(lastBackslash);
                    }
                }
            }

            // Replace forward slashes with backward slashes if needed to match your DB style
            // Note: Oracle and Java handle '/' better, but if you want '\', use:
            // pathForDB = pathForDB.replace('/', '\\');

            // Database Insert
            String sql = "INSERT INTO PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRODUCT_TYPE, STOCK, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idField.getText());
                ps.setString(2, nameField.getText());
                ps.setString(3, typeComboBox.getValue());
                ps.setInt(4, Integer.parseInt(stockField.getText()));
                ps.setDouble(5, Double.parseDouble(priceField.getText()));
                ps.setString(6, pathForDB); // This inserts "\MenuImages\IceCoffee.jpg"

                ps.executeUpdate();
                loadInventoryData();
                clearFields();
                showAlert("Success", "Path saved to DB: " + pathForDB, Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            showAlert("DB Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        // 1. Get the ID from the text field (The only mandatory field)
        String targetId = idField.getText();

        if (targetId == null || targetId.trim().isEmpty()) {
            showAlert("Error", "Please enter the Product ID to update", Alert.AlertType.ERROR);
            return;
        }

        // 2. Process image path using String functions for \MenuImages\ style
        String formattedImagePath = null;
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            int lastBackslash = selectedImagePath.lastIndexOf('\\');
            if (lastBackslash != -1) {
                int secondToLastBackslash = selectedImagePath.lastIndexOf('\\', lastBackslash - 1);
                if (secondToLastBackslash != -1) {
                    formattedImagePath = selectedImagePath.substring(secondToLastBackslash);
                }
            }
        }

        try (Connection conn = DBC.getInstance().getConnection()) {
            // 3. Use NVL in SQL to keep the old value if the new parameter is NULL
            // This prevents the ORA-01407 error because it won't try to insert NULL
            String sql = "UPDATE PRODUCT SET " +
                    "PRODUCT_NAME = NVL(?, PRODUCT_NAME), " +
                    "PRODUCT_TYPE = NVL(?, PRODUCT_TYPE), " +
                    "STOCK = NVL(?, STOCK), " +
                    "PRICE = NVL(?, PRICE), " +
                    "IMAGE = NVL(?, IMAGE) " +
                    "WHERE PRODUCT_ID = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Set Name (Pass null if empty, NVL will handle it)
                ps.setString(1, nameField.getText().isEmpty() ? null : nameField.getText());

                // Set Type (Pass null if no selection)
                ps.setString(2, typeComboBox.getValue() == null ? null : typeComboBox.getValue());

                // Set Stock
                if (stockField.getText().isEmpty()) {
                    ps.setNull(3, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(3, Integer.parseInt(stockField.getText()));
                }

                // Set Price
                if (priceField.getText().isEmpty()) {
                    ps.setNull(4, java.sql.Types.DOUBLE);
                } else {
                    ps.setDouble(4, Double.parseDouble(priceField.getText()));
                }

                // Set Image
                ps.setString(5, formattedImagePath);

                // The Key ID
                ps.setString(6, targetId);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    loadInventoryData(); // Refresh Table
                    clearFields();
                    showAlert("Success", "Product " + targetId + " updated successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Product ID not found: " + targetId, Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Stock and Price must be numbers", Alert.AlertType.ERROR);
        }
    }





    @FXML
    private void handleDelete(ActionEvent event) {
        Product selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a product to delete", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = DBC.getInstance().getConnection()) {
            String sql = "DELETE FROM PRODUCT WHERE PRODUCT_ID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selected.getId());
            ps.executeUpdate();
            loadInventoryData();
            clearFields();
            showAlert("Success", "Product deleted successfully", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("DB Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleImportImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            productImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void clearFields() {
        idField.clear();
        nameField.clear();
        stockField.clear();
        priceField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        productImageView.setImage(null);
        selectedImagePath = "";
    }


    // SEARCH product by id or name or type
    private void setupSearch() {
        FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(product -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return product.getId().toLowerCase().contains(lowerCaseFilter) ||
                        product.getName().toLowerCase().contains(lowerCaseFilter) ||
                        product.getType().toLowerCase().contains(lowerCaseFilter);
            });
        });
        inventoryTable.setItems(filteredData);
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

    @FXML void handleDashboard(ActionEvent e) { switchScene(e, "/view/Admin/Dashboard.fxml"); }
    @FXML void handleSales(ActionEvent e) { switchScene(e, "/view/Admin/Sales.fxml"); }
    @FXML void handleInventory(ActionEvent e) { switchScene(e, "/view/Admin/Inventory.fxml"); }
    @FXML void handleAddEmployee(ActionEvent e) { switchScene(e, "/view/Admin/AddEmployee.fxml"); }
    @FXML void handleUpdateEmployee(ActionEvent e) { switchScene(e, "/view/Admin/UpdateEmployee.fxml"); }
    @FXML void handleAbout(ActionEvent e) { switchScene(e, "/view/Admin/About.fxml"); }
    @FXML void handleSettings(ActionEvent e) { switchScene(e, "/view/Admin/Settings.fxml"); }
    @FXML void handleLogout(ActionEvent e) { switchScene(e, "/view/login.fxml"); }


    // ALERT METHOD
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
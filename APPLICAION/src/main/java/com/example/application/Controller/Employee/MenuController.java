package com.example.application.Controller.Employee;

import com.example.application.Database.DBC;
import com.example.application.Models.CartItem;
import com.example.application.Models.Product;
import com.example.application.Controller.BaseController;
import com.example.application.Models.ThemeManager;
import com.example.application.Models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MenuController extends BaseController {

    @FXML private GridPane menuGrid;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> itemColumn;
    @FXML private TableColumn<CartItem, Integer> qtyColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> customerComboBox;

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final ObservableList<String> customerNames = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        //to see actions immediately
        Platform.runLater(() -> {
            if (rootPane != null && rootPane.getScene() != null) {
                ThemeManager.setScene(rootPane.getScene());
                updateLogoForTheme();
            }
        });

        setupCartTable();
        loadCustomers();
        loadProductsFromDatabase();
    }

    private void setupCartTable() {
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        cartTable.setItems(cartItems);
    }

    private void loadCustomers() {
        customerNames.clear();
        try (Connection conn = DBC.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT NAME FROM CUSTOMER ORDER BY NAME")) {
            while (rs.next()) customerNames.add(rs.getString("NAME"));
            customerComboBox.setItems(customerNames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProductsFromDatabase() {
        menuGrid.getChildren().clear();
        int col = 0, row = 0;
        try (Connection conn = DBC.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM PRODUCT")) {
            while (rs.next()) {
                //to make the path valid
                String imagePath = rs.getString("IMAGE").replace("\\", "/");
                Product p = new Product(rs.getString("PRODUCT_ID"),
                        rs.getString("PRODUCT_NAME"),
                        rs.getString("PRODUCT_TYPE"),
                        rs.getInt("STOCK"),
                        rs.getDouble("PRICE"),
                        imagePath);

                //each product has card
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Employee/ProductsCardView.fxml"));
                VBox card = loader.load();//but in VBOX
                ProductCardController ctrl = loader.getController();//to enable to manage cards
                ctrl.setProduct(p);//determine 
                ctrl.setCartReference(cartItems, totalLabel, cartTable);

                menuGrid.add(card, col++, row);
                if (col == 3) { col = 0; row++; }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }


    @FXML private void handleCancel() {
        cartItems.clear();
        totalLabel.setText("Rs. 0.00");
    }

    @FXML
    private void handlePay() {
        // Check if cart is empty or no customer is selected
        if (cartItems.isEmpty() || customerComboBox.getValue() == null) {
            showAlert("Error", "Please select a customer and add items to cart!");
            return;
        }

        // Get the logged-in employee ID (e.g., E002)
        String currentEmpId = User.getEmployeeId();
        Connection conn = null;

        try {
            conn = DBC.getInstance().getConnection();
            // Start transaction to ensure data integrity
            conn.setAutoCommit(false);

            // Calculate the total price of all items in the cart
            double total = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();

            // 1. Insert into SALE table
            // Using PreparedStatement with RETURN_GENERATED_KEYS to avoid ORA-17173
            String sqlSale = "INSERT INTO SALE (TOTAL, SALE_DATE, CUS_ID, EMPLOYEE_ID) " +
                    "VALUES (?, SYSDATE, (SELECT CUS_ID FROM CUSTOMER WHERE NAME = ?), ?)";

            String generatedSaleId = null;

            // Specify "SALE_ID" as the column to be retrieved after insertion
            try (PreparedStatement psSale = conn.prepareStatement(sqlSale, new String[]{"SALE_ID"})) {
                psSale.setDouble(1, total);
                psSale.setString(2, customerComboBox.getValue());
                psSale.setString(3, currentEmpId);
                psSale.executeUpdate();

                // Retrieve the newly generated SALE_ID (e.g., S013)
                try (ResultSet rs = psSale.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedSaleId = rs.getString(1);
                    }
                }
            }

            // Verify that the Sale ID was successfully retrieved
            if (generatedSaleId == null) {
                throw new SQLException("Failed to retrieve generated SALE_ID from database.");
            }

            //  Insert items into SALE_DETAIL table
            String sqlDetail = "INSERT INTO SALE_DETAIL (SALE_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (CartItem item : cartItems) {
                    psDetail.setString(1, generatedSaleId);
                    psDetail.setString(2, item.getProductId());
                    psDetail.setInt(3, item.getQuantity());
                    psDetail.setDouble(4, item.getUnitPrice());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            // 3. Update stock quantity in PRODUCT table
            String sqlUpdateStock = "UPDATE PRODUCT SET STOCK = STOCK - ? WHERE PRODUCT_ID = ?";
            try (PreparedStatement psStock = conn.prepareStatement(sqlUpdateStock)) {
                for (CartItem item : cartItems) {
                    psStock.setInt(1, item.getQuantity());
                    psStock.setString(2, item.getProductId());
                    psStock.addBatch();
                }
                psStock.executeBatch();
            }

            // Commit all changes if everything is successful
            conn.commit();

            // Reset UI components and refresh product list
            handleCancel();
            loadProductsFromDatabase();

            showAlert("Success", "Payment successful! Sale and Details have been registered.");

        } catch (SQLException e) {
            // Rollback transaction in case of any database error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

   //Navigate to other pages
    private void switchScene(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            ThemeManager.setScene(scene);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleDashboard(ActionEvent e) { switchScene(e, "/view/Employee/DashboardEView.fxml"); }
    @FXML private void handleCustomer(ActionEvent e) { switchScene(e, "/view/Employee/CustomerView.fxml"); }
    @FXML private void handleAbout(ActionEvent e) { switchScene(e, "/view/Employee/AboutEView.fxml"); }
    @FXML private void handleSettings(ActionEvent e) { switchScene(e, "/view/Employee/SettingsEView.fxml"); }
    @FXML private void handleLogout(ActionEvent e) { switchScene(e, "/view/login.fxml"); }


}
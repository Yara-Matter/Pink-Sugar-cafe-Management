package com.example.application.Controller.Employee;

import com.example.application.Controller.BaseController;
import com.example.application.Database.DBC;
import com.example.application.Models.Customer;
import com.example.application.Models.ThemeManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class CustomerController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;
    @FXML private TextField idField, nameField, addressField, contactField;
    @FXML private Label statusLabel;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colId, colName, colAddress, colContact;

    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        updateLogoForTheme();
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getId()));
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        colAddress.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getAddress()));
        colContact.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getContact()));

        customerTable.setItems(customerList);
        loadCustomers();
    }

    public void updateLogoForTheme() {
        if (logoImageView == null) return;
        String path = ThemeManager.isDarkMode() ? "/images/logo.png" : "/images/light.png";
        logoImageView.setImage(new Image(getClass().getResourceAsStream(path)));
    }

    private void loadCustomers() {
        customerList.clear();
        String sql = "SELECT CUS_ID, NAME, ADDRESS, CONTACT_NUMBER FROM CUSTOMER ORDER BY CUS_ID";
        try (Connection conn = DBC.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                customerList.add(new Customer(
                        rs.getString("CUS_ID"), rs.getString("NAME"),
                        rs.getString("ADDRESS"), rs.getString("CONTACT_NUMBER")
                ));
            }
        } catch (SQLException e) { showAlert("DB Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML
    private void saveCustomer(ActionEvent event) {
        if (idField.getText().isEmpty() || nameField.getText().isEmpty() ||
                addressField.getText().isEmpty() || contactField.getText().isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        String sql = "INSERT INTO CUSTOMER (CUS_ID, NAME, ADDRESS, CONTACT_NUMBER) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBC.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idField.getText().toUpperCase());
            ps.setString(2, nameField.getText());
            ps.setString(3, addressField.getText());
            ps.setString(4, contactField.getText());

            ps.executeUpdate();
            loadCustomers();
            clearFields();
            statusLabel.setText("Customer added successfully");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) { showAlert("DB Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    private void clearFields() {
        idField.clear(); nameField.clear(); addressField.clear(); contactField.clear();
    }


    private void switchScene(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
            ThemeManager.setScene(scene);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void handleDashboard(ActionEvent e){ switchScene(e,"/view/Employee/DashboardEView.fxml"); }
    @FXML void handleMenu(ActionEvent e){ switchScene(e,"/view/Employee/MenuView.fxml"); }
    @FXML void handleCustomer(ActionEvent e){ switchScene(e,"/view/Employee/CustomerView.fxml"); }
    @FXML void handleAbout(ActionEvent e){ switchScene(e,"/view/Employee/AboutEView.fxml"); }
    @FXML void handleSettings(ActionEvent e){ switchScene(e, "/view/Employee/SettingsEView.fxml"); }
    @FXML void handleLogout(ActionEvent e){ switchScene(e,"/view/login.fxml"); }

    private void showAlert(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}
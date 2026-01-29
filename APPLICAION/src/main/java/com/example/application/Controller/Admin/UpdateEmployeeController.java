package com.example.application.Controller.Admin;

import com.example.application.Controller.BaseController;
import com.example.application.Database.DBC;
import com.example.application.Models.Employee;
import com.example.application.Models.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class UpdateEmployeeController extends BaseController {

    // FXML Elements
    @FXML private BorderPane rootPane;
    @FXML private TextField searchIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField contactField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker createdAtPicker;
    @FXML private Label statusLabel;

    // TableView
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> empIdCol;
    @FXML private TableColumn<Employee, String> firstNameCol;
    @FXML private TableColumn<Employee, String> lastNameCol;
    @FXML private TableColumn<Employee, String> contactCol;
    @FXML private TableColumn<Employee, String> positionCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, String> createdAtCol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            super.initialize(url, rb);
            updateFontSize(ThemeManager.getCurrentFontSize());


            // ComboBox values
            roleComboBox.getItems().addAll("admin", "employee");
            statusComboBox.getItems().addAll("active", "inactive");
            statusLabel.setText("Ready.");

            // TableView columns
            empIdCol.setCellValueFactory(cellData -> cellData.getValue().empIdProperty());
            firstNameCol.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
            lastNameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
            contactCol.setCellValueFactory(cellData -> cellData.getValue().contactNumberProperty());
            positionCol.setCellValueFactory(cellData -> cellData.getValue().positionProperty());
            statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            createdAtCol.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty().asString());

            loadEmployees();

        } catch (Exception e) {
            showAlert("Initialization Error", "Something went wrong during initialization:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Apply current font size
    private void updateFontSize(double size) {
        try {
            if (rootPane != null)
                rootPane.setStyle("-fx-font-size: " + size + "px;");
        } catch (Exception e) {
            showAlert("UI Error", "Failed to apply font size:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }




    // Load all employees into TableView
    private void loadEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        String sql = "SELECT EMP_ID, FIRST_NAME, LAST_NAME, CONTACT_NUMBER, POSITION, STATUS, CREATED_AT FROM EMPLOYEE ORDER BY EMP_ID";

        try (Connection conn = DBC.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LocalDate createdDate = null;
                Date date = rs.getDate("CREATED_AT");
                if (date != null) createdDate = date.toLocalDate();

                employees.add(new Employee(
                        rs.getString("EMP_ID"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"),
                        rs.getString("CONTACT_NUMBER"),
                        rs.getString("POSITION"),
                        rs.getString("STATUS"),
                        createdDate
                ));
            }

            employeeTable.setItems(employees);

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load employees:\n" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Unexpected Error", "An error occurred while loading employees:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Search employee by ID or Name and fill the update form
    @FXML
    private void searchEmployee(ActionEvent event) {
        try {
            String filter = searchIdField.getText().trim();
            if (filter.isEmpty()) {
                showAlert("Input Error", "Please enter an Employee ID or Name to search.", Alert.AlertType.WARNING);
                return;
            }

            String sql = "SELECT * FROM EMPLOYEE WHERE EMP_ID = ? OR FIRST_NAME = ?";
            try (Connection conn = DBC.getInstance().getConnection();
                 PreparedStatement pstm = conn.prepareStatement(sql)) {

                pstm.setString(1, filter);
                pstm.setString(2, filter);
                ResultSet rs = pstm.executeQuery();

                if (rs.next()) {
                    searchIdField.setText(rs.getString("EMP_ID"));
                    firstNameField.setText(rs.getString("FIRST_NAME"));
                    lastNameField.setText(rs.getString("LAST_NAME"));
                    contactField.setText(rs.getString("CONTACT_NUMBER"));
                    passwordField.setText(rs.getString("PASSWORD"));
                    roleComboBox.setValue(rs.getString("POSITION").toLowerCase());
                    statusComboBox.setValue(rs.getString("STATUS").toLowerCase());
                    if (rs.getDate("CREATED_AT") != null)
                        createdAtPicker.setValue(rs.getDate("CREATED_AT").toLocalDate());

                    statusLabel.setText("Employee found. Update enabled.");
                    statusLabel.setStyle("-fx-text-fill: #00b894; -fx-font-weight: bold;");
                } else {
                    showAlert("Not Found", "No employee found matching: " + filter, Alert.AlertType.INFORMATION);
                    clearFields();
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching employee data:\n" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Update employee record in database
    @FXML
    private void updateRecord(ActionEvent event) {
        try {
            String id = searchIdField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String contact = contactField.getText().trim();
            String password = passwordField.getText();
            String role = roleComboBox.getValue();
            String status = statusComboBox.getValue();
            LocalDate createdAt = createdAtPicker.getValue();

            if (firstName.isEmpty() || lastName.isEmpty() || contact.isEmpty() ||
                    password.isEmpty() || role == null || status == null || createdAt == null) {
                showAlert("Validation Error", "All fields are required.", Alert.AlertType.ERROR);
                return;
            }

            // Contact Regex
            String contactRegex = "^01[0125][0-9]{8}$";
            if (!contact.matches(contactRegex)) {
                showAlert(
                        "Invalid Contact Number",
                        "Contact number must start with 01, followed by 0 / 1 / 2 / 5 and 8 digits.",
                        Alert.AlertType.ERROR
                );
                return;
            }

            // Password Regex
            if (!password.matches("^[A-Za-z]{3,}[0-9]{3,}$")) {
                showAlert(
                        "Validation Error",
                        "Password must start with at least 3 letters followed by at least 3 digits.",
                        Alert.AlertType.ERROR
                );
                return;
            }

            String sql = "UPDATE EMPLOYEE SET FIRST_NAME = ?, LAST_NAME = ?, CONTACT_NUMBER = ?, PASSWORD = ?, POSITION = ?, STATUS = ?, CREATED_AT = ? WHERE EMP_ID = ?";

            try (Connection conn = DBC.getInstance().getConnection();
                 PreparedStatement pstm = conn.prepareStatement(sql)) {

                pstm.setString(1, firstName);
                pstm.setString(2, lastName);
                pstm.setString(3, contact);
                pstm.setString(4, password);
                pstm.setString(5, role);
                pstm.setString(6, status);
                pstm.setDate(7, Date.valueOf(createdAt));
                pstm.setString(8, id);

                int affectedRows = pstm.executeUpdate();

                if (affectedRows > 0) {
                    statusLabel.setText("Update Successful!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    showAlert("Success", "Employee records updated successfully.", Alert.AlertType.INFORMATION);
                    loadEmployees();
                } else {
                    showAlert("Error", "Could not update. Make sure the Employee ID is correct.", Alert.AlertType.ERROR);
                }
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Update failed:\n" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    /** Clear all form fields */
    private void clearFields() {
        try {
            searchIdField.clear();
            firstNameField.clear();
            lastNameField.clear();
            contactField.clear();
            passwordField.clear();
            roleComboBox.getSelectionModel().clearSelection();
            statusComboBox.getSelectionModel().clearSelection();
            createdAtPicker.setValue(null);
            statusLabel.setText("Ready.");
            statusLabel.setStyle("-fx-text-fill: black;");
        } catch (Exception e) {
            showAlert("UI Error", "Failed to clear fields:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /** Show alert messages */
    private void showAlert(String title, String content, Alert.AlertType type) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Navigation Methods ---
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
            showAlert("Scene Load Error", "Cannot load FXML: " + fxmlPath + "\n" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Unexpected Error", "An unexpected error occurred:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML void handleDashboard(ActionEvent event) { switchScene(event, "/view/Admin/Dashboard.fxml"); }
    @FXML void handleLogout(ActionEvent event) { switchScene(event, "/view/login.fxml"); }
    @FXML void handleSales(ActionEvent event) { switchScene(event, "/view/Admin/Sales.fxml"); }
    @FXML void handleInventory(ActionEvent event) { switchScene(event, "/view/Admin/Inventory.fxml"); }
    @FXML void handleAddEmployee(ActionEvent event) { switchScene(event, "/view/Admin/AddEmployee.fxml"); }
    @FXML void handleUpdateEmployee(ActionEvent event) { switchScene(event, "/view/Admin/UpdateEmployee.fxml"); }
    @FXML void handleAbout(ActionEvent event) { switchScene(event, "/view/Admin/About.fxml"); }
    @FXML void handleSettings(ActionEvent event) { switchScene(event, "/view/Admin/Settings.fxml"); }

}

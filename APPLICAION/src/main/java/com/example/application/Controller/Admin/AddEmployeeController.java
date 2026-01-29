package com.example.application.Controller.Admin;

import com.example.application.Controller.BaseController;
import com.example.application.Database.DBC;
import com.example.application.Models.Employee;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AddEmployeeController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;
    @FXML private TextField idField, firstNameField, lastNameField, contactField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> positionComboBox, statusComboBox;
    @FXML private DatePicker createdAtPicker;
    @FXML private Label statusLabel;

    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, String> colId, colFirstName, colLastName, colContact, colPosition, colStatus, colCreatedAt;

    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
        updateLogoForTheme();
        createdAtPicker.setValue(LocalDate.now());

        // ComboBoxes
        positionComboBox.getItems().addAll("admin", "employee");
        statusComboBox.getItems().addAll("active", "inactive");

        // Table Columns
        colId.setCellValueFactory(e -> new ReadOnlyStringWrapper(e.getValue().getEmpId()));
        colFirstName.setCellValueFactory(e -> new ReadOnlyStringWrapper(e.getValue().getFirstName()));
        colLastName.setCellValueFactory(e -> new ReadOnlyStringWrapper(e.getValue().getLastName()));
        colContact.setCellValueFactory(e -> new ReadOnlyStringWrapper(e.getValue().getContactNumber()));
        colPosition.setCellValueFactory(e -> new ReadOnlyStringWrapper(e.getValue().getPosition()));
        colStatus.setCellValueFactory(e -> new ReadOnlyStringWrapper(e.getValue().getStatus()));
        colCreatedAt.setCellValueFactory(e -> {
            LocalDate d = e.getValue().getCreatedAt();
            return new ReadOnlyStringWrapper(d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });

        employeesTable.setItems(employeeList);
        loadEmployees();
    }

    public void updateLogoForTheme() {
        if (logoImageView == null) return;
        String path = ThemeManager.isDarkMode() ? "/images/logo.png" : "/images/light.png";
        logoImageView.setImage(new Image(getClass().getResourceAsStream(path)));
    }

    private void loadEmployees() {
        employeeList.clear();
        String sql = "SELECT EMP_ID, FIRST_NAME, LAST_NAME, CONTACT_NUMBER, POSITION, STATUS, CREATED_AT FROM EMPLOYEE ORDER BY EMP_ID";
        try (Connection conn = DBC.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                employeeList.add(new Employee(
                        rs.getString("EMP_ID"), rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"), rs.getString("CONTACT_NUMBER"),
                        rs.getString("POSITION"), rs.getString("STATUS"),
                        rs.getDate("CREATED_AT").toLocalDate()
                ));
            }
        } catch (SQLException e) { showAlert("DB Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    @FXML
    private void saveEmployee(ActionEvent event) {

        if (idField.getText().isEmpty() || firstNameField.getText().isEmpty() ||
                lastNameField.getText().isEmpty() || contactField.getText().isEmpty() ||
                passwordField.getText().isEmpty() || positionComboBox.getValue() == null ||
                statusComboBox.getValue() == null || createdAtPicker.getValue() == null) {

            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        //  REGEX  validation
        String contactRegex = "^01[0125][0-9]{8}$";
        String idRegex = "^E[0-9]{3}$";

        if (!contactField.getText().matches(contactRegex)) {
            showAlert("Invalid Contact Number",
                    "Contact number must start with 01, followed by 0/1/2/5 and 8 digits",
                    Alert.AlertType.ERROR);
            return;
        }

        if (!idField.getText().toUpperCase().matches(idRegex)) {
            showAlert("Invalid Employee ID",
                    "Employee ID must be in this format: E123 ,E followed by 3 digits",
                    Alert.AlertType.ERROR);
            return;
        }

        // DB 
        try (Connection conn = DBC.getInstance().getConnection()) {

            String sql = "INSERT INTO EMPLOYEE (EMP_ID, FIRST_NAME, LAST_NAME, CONTACT_NUMBER, PASSWORD, POSITION, STATUS, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, idField.getText().toUpperCase());
            ps.setString(2, firstNameField.getText());
            ps.setString(3, lastNameField.getText());
            ps.setString(4, contactField.getText());
            ps.setString(5, passwordField.getText());
            ps.setString(6, positionComboBox.getValue());
            ps.setString(7, statusComboBox.getValue());
            ps.setDate(8, java.sql.Date.valueOf(createdAtPicker.getValue()));

            ps.executeUpdate();
            loadEmployees();
            clearFields();
            statusLabel.setText("Employee added successfully");
            statusLabel.setStyle("-fx-text-fill: green;");

        } catch (SQLException e) {
            showAlert("DB Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    private void clearFields() {
        idField.clear(); firstNameField.clear(); lastNameField.clear(); contactField.clear();
        passwordField.clear(); positionComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        createdAtPicker.setValue(LocalDate.now());
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
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

    private void showAlert(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}

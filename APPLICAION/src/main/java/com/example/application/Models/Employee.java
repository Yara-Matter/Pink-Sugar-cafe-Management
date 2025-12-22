package com.example.application.Models;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

public class Employee {

    private final SimpleStringProperty empId = new SimpleStringProperty(this, "empId", "");
    private final SimpleStringProperty firstName = new SimpleStringProperty(this, "firstName", "");
    private final SimpleStringProperty lastName = new SimpleStringProperty(this, "lastName", "");
    private final SimpleStringProperty contactNumber = new SimpleStringProperty(this, "contactNumber", "");
    private final SimpleStringProperty position = new SimpleStringProperty(this, "position", "");
    private final SimpleStringProperty status = new SimpleStringProperty(this, "status", "");
    private final SimpleObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>(this, "createdAt");

    public Employee(String empId, String firstName, String lastName, String contactNumber,
                    String position, String status, LocalDate createdAt) {
        this.empId.set(empId);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.contactNumber.set(contactNumber);
        this.position.set(position);
        this.status.set(status);
        this.createdAt.set(createdAt);
    }

    public SimpleStringProperty empIdProperty() { return empId; }
    public SimpleStringProperty firstNameProperty() { return firstName; }
    public SimpleStringProperty lastNameProperty() { return lastName; }
    public SimpleStringProperty contactNumberProperty() { return contactNumber; }
    public SimpleStringProperty positionProperty() { return position; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleObjectProperty<LocalDate> createdAtProperty() { return createdAt; }

    public String getEmpId() { return empId.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getContactNumber() { return contactNumber.get(); }
    public String getPosition() { return position.get(); }
    public String getStatus() { return status.get(); }
    public LocalDate getCreatedAt() { return createdAt.get(); }
}
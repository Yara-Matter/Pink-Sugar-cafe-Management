package com.example.application.Models;

import javafx.beans.property.*;

public class Sale {
    private final StringProperty saleId;
    private final StringProperty customerName;
    private final StringProperty contact;
    private final DoubleProperty total;
    private final StringProperty date;

    public Sale(String saleId, String customerName, String contact, double total, String date) {
        this.saleId = new SimpleStringProperty(saleId);
        this.customerName = new SimpleStringProperty(customerName);
        this.contact = new SimpleStringProperty(contact);
        this.total = new SimpleDoubleProperty(total);
        this.date = new SimpleStringProperty(date);
    }

    // getters for TableView
    public StringProperty saleIdProperty() { return saleId; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty contactProperty() { return contact; }
    public DoubleProperty totalProperty() { return total; }
    public StringProperty dateProperty() { return date; }

    // optional: regular getters
    public String getSaleId() { return saleId.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getContact() { return contact.get(); }
    public double getTotal() { return total.get(); }
    public String getDate() { return date.get(); }
}

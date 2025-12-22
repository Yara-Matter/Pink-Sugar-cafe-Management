package com.example.application.Models;

public class Customer {
    private String id, name, address, contact;

    public Customer(String id, String name, String address, String contact) {
        this.id = id; this.name = name; this.address = address; this.contact = contact;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getContact() { return contact; }
}
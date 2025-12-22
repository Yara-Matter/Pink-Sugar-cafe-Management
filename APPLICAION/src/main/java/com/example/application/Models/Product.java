package com.example.application.Models;

public class Product {
    private String id;
    private String name;
    private String type;
    private int stock;
    private double price;
    private String imagePath;

    public Product(String id, String name, String type, int stock, double price, String imagePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.stock = stock;
        this.price = price;
        this.imagePath = imagePath;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getStock() { return stock; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
}
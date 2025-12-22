package com.example.application.Controller.Employee;

import com.example.application.Models.CartItem;
import com.example.application.Models.Product;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;


import java.net.URL;

public class ProductCardController {

    @FXML private ImageView productImage;
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Spinner<Integer> quantitySpinner;

    private Product product;
    private ObservableList<CartItem> cartItems;
    private Label totalLabel;
    private TableView<CartItem> cartTable;

    public void setProduct(Product product) {
        this.product = product;
        updateCardDisplay(); // Populate the card as soon as we receive the Product
    }

    public void setCartReference(ObservableList<CartItem> cartItems, Label totalLabel, TableView<CartItem> cartTable) {
        this.cartItems = cartItems;
        this.totalLabel = totalLabel;
        this.cartTable = cartTable;
    }

    // Method to populate the card with product data
    private void updateCardDisplay() {
        if (product == null) return;

        productName.setText(product.getName());
        productPrice.setText("Rs. " + String.format("%.2f", product.getPrice()));

        // Spinner handling based on stock
        if (product.getStock() <= 0) {
            quantitySpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0)
            );
            quantitySpinner.setDisable(true);
        } else {
            quantitySpinner.setDisable(false);
            quantitySpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, product.getStock(), 1)
            );
        }

        // Load image
        String imagePath = product.getImagePath();

        //this to just make sure from image sources
       // System.out.println("Image path from database for " + product.getName() + ": " + imagePath);

        Image image = null;

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            URL url = getClass().getResource(imagePath);
            if (url != null) {
                image = new Image(url.toExternalForm(), true);
            } else {
                System.out.println("Image not found: " + imagePath);
            }
        }

        // Default image fallback
        if (image == null) {
            URL defaultUrl = getClass().getResource("/MenuImages/default.png");
            if (defaultUrl != null) {
                image = new Image(defaultUrl.toExternalForm(), true);
            } else {
                image = new Image("https://via.placeholder.com/150x150.png?text=No+Image");
            }
        }
        productImage.setImage(image);
    }


    @FXML
    private void handleAdd() {
        if (product == null) return;

        int quantity = quantitySpinner.getValue();

        if (quantity > product.getStock()) {
            showAlert("Not Enough Stock", "Only " + product.getStock() + " units available");
            return;
        }

        CartItem existing = cartItems.stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            if (newQty > product.getStock()) {
                showAlert("Exceeds Stock", "Cannot add this quantity. Available: " + product.getStock());
                return;
            }
            existing.setQuantity(newQty);
        } else {
            CartItem newItem = new CartItem(product.getId(), product.getName(), quantity, product.getPrice());
            cartItems.add(newItem);
        }

        updateTotal();
        cartTable.refresh();
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
        totalLabel.setText("Rs. " + String.format("%.2f", total));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleDelete(ActionEvent event) {
            if (product == null) return;

            // delete from card
            cartItems.removeIf(item -> item.getProductId().equals(product.getId()));

            // total
            if (cartTable != null) {
                cartTable.refresh();
            }
            updateTotal();

    }
}

package com.example.application.Controller;

import com.example.application.Models.ThemeManager;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseController implements Initializable { //abstract

    //elements i need to edit
    @FXML protected ImageView logoImageView;
    @FXML protected BorderPane rootPane;

    private ListChangeListener<String> styleClassListener; //listener to monitor the changes in
    private boolean listenerAdded = false; // to avoid use more than one listener

    protected void updateLogoForTheme() {
        if (logoImageView == null) return;
        String logoPath = ThemeManager.isDarkMode() ? "/images/logo.png" : "/images/light.png";
        try {
            Image logoImage = new Image(getClass().getResourceAsStream(logoPath));
            if (!logoImage.isError()) {
                logoImageView.setImage(logoImage);
            } else {
                System.err.println("Failed to load image: " + logoPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + logoPath);
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateLogoForTheme();

        Platform.runLater(() -> {
            if (rootPane != null && rootPane.getScene() != null && !listenerAdded) {
                ThemeManager.setScene(rootPane.getScene());

                styleClassListener = change -> { //listener to monitor the change of the scene
                    while (change.next()) {
                        if (change.wasAdded() || change.wasRemoved()) {
                            updateLogoForTheme();
                        }
                    }
                };

                rootPane.getStyleClass().addListener(styleClassListener);
                rootPane.getScene().getRoot().getStyleClass().addListener(styleClassListener);

                listenerAdded = true;
            }
        });
    }
}

package com.example.application.Models;

public class AppConfig {

    private static AppConfig instance;
    private boolean darkMode = false;
    private double fontSize = 15; // Default font size

    private AppConfig() {}

    public static AppConfig getInstance() {
        if (instance == null)
            instance = new AppConfig();
        return instance;
    }

    // Dark mode
    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    // Font size
    public double getFontSize() {
        return fontSize;
    }


    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
}

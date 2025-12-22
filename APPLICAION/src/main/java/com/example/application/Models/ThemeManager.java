package com.example.application.Models;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String DARK_CSS = "/styles/dark.css";
    private static final String LIGHT_CSS = "/styles/light.css";
    private static final String THEME_KEY = "theme";
    private static final String FONT_SIZE_KEY = "font_size";

    private static Scene mainScene; //only one scene used
    private static Preferences prefs = Preferences.userRoot().node("myapp/settings");//to save settings

    public static void setScene(Scene scene) {
        mainScene = scene;
        applySavedSettings();
    }

    public static void applySavedSettings() {
        if (mainScene == null) return;
        switchTheme(isDarkMode());
        applyFontSize(prefs.getDouble(FONT_SIZE_KEY, 14.0));
    }

    public static void switchTheme(boolean darkMode) {
        if (mainScene == null) return;
        ObservableList<String> stylesheets = mainScene.getStylesheets();
        stylesheets.clear();
        try {
            String cssPath = darkMode ? DARK_CSS : LIGHT_CSS;
            stylesheets.add(ThemeManager.class.getResource(cssPath).toExternalForm());
            mainScene.getRoot().getStyleClass().removeAll("light", "dark");
            mainScene.getRoot().getStyleClass().add(darkMode ? "dark" : "light");
            prefs.put(THEME_KEY, darkMode ? "dark" : "light");
        } catch (Exception e) { System.err.println("CSS Error!"); }
    }

    public static void applyFontSize(double fontSize) {
        if (mainScene != null) {
            mainScene.getRoot().setStyle(String.format("-fx-font-size: %.1fpx;", fontSize));
        }
        prefs.putDouble(FONT_SIZE_KEY, fontSize);
    }

    public static boolean isDarkMode() {
        return prefs.get(THEME_KEY, "light").equals("dark");
    }

    public static double getCurrentFontSize() {
        return prefs.getDouble(FONT_SIZE_KEY, 14.0);
    }
}
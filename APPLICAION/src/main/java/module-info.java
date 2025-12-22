module com.example.application {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.prefs;

    opens com.example.application to javafx.fxml;

    opens com.example.application.Controller.Admin to javafx.fxml;
    opens com.example.application.Controller.Employee to javafx.fxml;
    opens com.example.application.Controller to javafx.fxml;

    opens com.example.application.Models to javafx.base, javafx.fxml;

    exports com.example.application;
}
package com.example.application.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBC {
    private static DBC instance; //one instance once project runed
    private Connection connection; //connection object

    private String url = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    private String user = "system";
    private String password = "yara";

    private DBC() { //private constructor
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");//to enable java connect with oracle
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DBC getInstance() { //if there is one instance then use it ,otherwise make an instace for usage
        if (instance == null) {
            instance = new DBC();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
}
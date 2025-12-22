package com.example.application.Models;

public class User {
    private static String employeeId;

    public static void setEmployeeId(String id) {
        employeeId = id;
    }

    public static String getEmployeeId() {
        return employeeId;
    }
}

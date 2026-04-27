package com.example;

import java.sql.Connection;

public class DatabaseUtil {

    public static Connection getConnection() throws Exception {
        // Normally returns a real connection, but we'll mock this in tests
        return null;
    }
}

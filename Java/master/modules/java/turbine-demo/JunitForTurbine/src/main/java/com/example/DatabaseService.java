package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseService {

    public String fetchData() throws Exception {
        Connection connection = DatabaseUtil.getConnection();
        assert connection != null;
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT name FROM users");
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getString("name");
        }
        return null;
    }
}




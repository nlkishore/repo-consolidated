package com.example.SpringMVC.Models;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper implements RowMapper<Product> {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product prod = new Product();
        prod.setId(rs.getInt("id"));
        prod.setName(rs.getString("name"));
        prod.setPrice(rs.getDouble("price"));
        return prod;
    }
}

package com.example.JDBC_with_Spring.Models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;

@Component
public class ProductDAO {
	private JdbcTemplate jdbctemplate;
	
	@Autowired
	public ProductDAO (JdbcTemplate jdbctemplate) {
		this.jdbctemplate = jdbctemplate;
	}

	public List<Product> GetAllProducts(){
		String sql = "select * from Products ";
		
		return this.jdbctemplate.execute(sql,new PreparedStatementCallback<List<Product>>() {

			@Override
			public List<Product> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				// TODO Auto-generated method stub
				ResultSet rs = ps.executeQuery();
				List<Product> prods = new ArrayList<Product>();
				int i=0;
				while (rs.next()) {
					prods.add(new ProductRowMapper().mapRow(rs, i));
					i++;
				}
				return prods;
			}
		});
	}
}

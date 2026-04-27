package com.example.SpringMVC.Models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

//import com.example.JDBC_with_Spring.Models.Product;
//import com.example.JDBC_with_Spring.Models.ProductRowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Component
//@ComponentScan({"com.example.SpringMVC.Models"})
public class ProductDAO {
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ProductDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void Add(Product prod) {
		String query = "insert into Products(id,name,price)values(?,?,?)";
		int count = jdbcTemplate.update(query, prod.getId(), prod.getName(), prod.getPrice());
		System.out.println("Number of records inserted " + count);
	}

	public List<Product> GetProducts() {
		String sqlquery = "select * from products";
		List<Product> prods = jdbcTemplate.query(sqlquery, new ProductRowMapper());
		System.out.println("---------------------------------");
		System.out.println("Rows Available:" + prods.size());
		System.out.println("---------------------------------");
		return prods;
	}

	public void Update(Product prod) {
		String query = "update products set name=?,price=? where id=?";
		int _rows = jdbcTemplate.update(query, prod.getName(), prod.getPrice(), prod.getId());
		System.out.println(_rows + " Record(s) Updated Successfully!");
	}

	/*
	 * public void delete(int id) { String sql = "delete from products where id=" +
	 * id; jdbcTemplate.execute(sql); }
	 */

	public Product getProduct(int id) {
		String sqlquery = "select * from products where id=" + id;
		Product prod = jdbcTemplate.query(sqlquery, new ProductRowMapper()).get(0);
		return prod;
	}

	/**
	 * Fetching Data Using Prepared Statement
	 * 
	 * @return
	 */
	public List<Product> GetAllProducts() {
		String sql = "select * from Products ";

		return this.jdbcTemplate.execute(sql, new PreparedStatementCallback<List<Product>>() {

			@Override
			public List<Product> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				// TODO Auto-generated method stub
				ResultSet rs = ps.executeQuery();
				List<Product> prods = new ArrayList<Product>();
				int i = 0;
				while (rs.next()) {
					prods.add(new ProductRowMapper().mapRow(rs, i));
					i++;
				}
				return prods;
			}
		});
	}

	/**
	 * Insert using Prepared Statement
	 * 
	 * @param prod
	 * @return
	 */
	public int insert(final Product prod) {
		String sql = "insert into products(id,name,price) values(?,?,?)";
		return jdbcTemplate.update(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, prod.getId());
				ps.setString(2, prod.getName());
				ps.setDouble(3, prod.getPrice());
			}
		});
	}

	public Product GetProductById(final int id) {
		String sql = "select * from products where id=?";
		return jdbcTemplate.execute(sql, new PreparedStatementCallback<Product>() {

			@Override
			public Product doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					return new ProductRowMapper().mapRow(rs, id);
				}
				return new Product();

			}

		});
	}

	public int update(final Product prod) {
		String sql = "update products set name=?,price=? where id=?";
		return jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(3, prod.getId());
				ps.setString(1, prod.getName());
				ps.setDouble(2, prod.getPrice());
			}
		});

	}

	public int delete(final int id) {
		String sql = "delete from products where id=?";
		return jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, id);
			}
		});

	}
}

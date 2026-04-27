package com.example.JDBC_with_Spring.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ComponentScan("com.example.JDBC_with_Spring.Models")
public class AppConfig {
	@Bean
	public DataSource datasource()
	{
		DriverManagerDataSource ds=new DriverManagerDataSource();
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost:3306/StoreDB");
		ds.setUsername("Kishore");
		ds.setPassword("Kish1381");
		return ds;
	}
	
	@Bean
	@Autowired
	public JdbcTemplate jdbctemplate(DataSource ds)
	{
		return new JdbcTemplate(ds);
	}


}

package com.example.SpringMVC.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@ComponentScan({"com.example.SpringMVC.Models"})
public class AppConfig
{

    @Bean
    public DataSource datasource(){
        DriverManagerDataSource   datasource = new DriverManagerDataSource();
        datasource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        datasource.setUrl("jdbc:mysql://localhost:3306/StoreDB");
        datasource.setUsername("Kishore");
        datasource.setPassword("Kish1381");//orcl
        return datasource;
    }
    @Bean
	@Autowired
	public JdbcTemplate jdbctemplate(DataSource ds)
	{
		return new JdbcTemplate(ds);
	}

}

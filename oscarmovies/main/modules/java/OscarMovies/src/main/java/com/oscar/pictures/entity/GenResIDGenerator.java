package com.oscar.pictures.entity;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("serial")
public class GenResIDGenerator implements IdentifierGenerator {
	@Override
	public Object generate(SharedSessionContractImplementor session, Object object){

	    String prefix = "g";
	JdbcConnectionAccess con = session.getJdbcConnectionAccess();
	        
	            try {
	                JdbcConnectionAccess jdbcConnectionAccess = session.getJdbcConnectionAccess();
	                Connection connection = jdbcConnectionAccess.obtainConnection();
	                Statement statement = connection.createStatement();
	                String query = "select count(gen_id) as Id from genres";

	                ResultSet resultSet = statement.executeQuery(query);

	                if (resultSet.next()) {
	                     int id=resultSet.getInt(1)+101;
	                     String generatedId = prefix + new Integer(id).toString();
	                     return generatedId;
	                }

	                resultSet.close();
	                statement.close();
	                connection.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	return null;
	}
	
}


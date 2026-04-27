package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @InjectMocks
    private DatabaseService databaseService;


    @BeforeEach
    public void setUp() throws Exception {
        try (MockedStatic<DatabaseUtil> mockedStatic = Mockito.mockStatic(DatabaseUtil.class)) {
            mockedStatic.when(DatabaseUtil::getConnection).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        }
    }


    public String fetchData() throws Exception {
        Connection connection = DatabaseUtil.getConnection(); // This should now return the mocked connection.
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT name FROM users");
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getString("name");
        }
        return null;
    }

}

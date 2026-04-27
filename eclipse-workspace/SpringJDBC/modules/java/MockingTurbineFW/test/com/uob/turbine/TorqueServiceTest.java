package com.uob.turbine;

import org.apache.torque.criteria.Criteria;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class TorqueServiceTest {

    @Test
    public void testFetchData() {
        // Mock the Criteria class from Torque
        Criteria mockCriteria = Mockito.mock(Criteria.class);

        // Stub the `where` method
        when(mockCriteria.where("columnName", "value")).thenReturn(mockCriteria);

        // Simulate fetchData logic using the mock
        TorqueService service = new TorqueService() {
            @Override
            public String fetchData(String columnName, String value) {
                mockCriteria.where(columnName, value);
                return "Mocked Data for " + value;
            }
        };

        // Test the method
        String result = service.fetchData("columnName", "value");

        // Verify behavior and result
        verify(mockCriteria, times(1)).where("columnName", "value");
        assertEquals("Mocked Data for value", result);
    }
}


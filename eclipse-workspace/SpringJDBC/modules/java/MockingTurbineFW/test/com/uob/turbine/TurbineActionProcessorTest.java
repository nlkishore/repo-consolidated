package com.uob.turbine;

import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ParameterParser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class TurbineActionProcessorTest {

    @Test
    public void testProcessRequest() {
        // Mock RunData and ParameterParser
        RunData mockRunData = Mockito.mock(RunData.class);
        ParameterParser mockParameters = Mockito.mock(ParameterParser.class);

        // Stub the ParameterParser methods
        when(mockRunData.getParameters()).thenReturn(mockParameters);
        when(mockParameters.getString("key", "default")).thenReturn("mockedValue");

        // Create an instance of the action processor
        TurbineActionProcessor processor = new TurbineActionProcessor();

        // Test the method
        String result = processor.processRequest(mockRunData);

        // Verify behavior and result
        verify(mockRunData, times(1)).getParameters();
        verify(mockParameters, times(1)).getString("key", "default");
        assertEquals("Processed Request with parameter: mockedValue", result);
    }
}

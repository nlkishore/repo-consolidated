package com.example;

import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.util.RunData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class StringManipulatorTest {

    @Test
    public void testFixString() {
        // Mock RunData
        RunData mockRunData = mock(RunData.class);

        // Mock ParameterParser
        ParameterParser mockParameters = mock(ParameterParser.class);

        // Define behavior
        when(mockRunData.getParameters()).thenReturn(mockParameters);
        when(mockParameters.getString("testKey")).thenReturn("   hello world   ");

        // Call the method to test
        StringManipulator manipulator = new StringManipulator();
        String result = manipulator.fixString(mockRunData, "testKey");

        // Assertions
        assertEquals("HELLO WORLD", result);

        // Verify interactions
        verify(mockRunData, times(1)).getParameters();
        verify(mockParameters, times(1)).getString("testKey");
    }
}

package com.example;

import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.util.RunData;

public class StringManipulator {

    public String fixString(RunData runData, String key) {
        ParameterParser params = runData.getParameters();
        String value = params.getString(key).trim();
        return value.toUpperCase();
    }
}

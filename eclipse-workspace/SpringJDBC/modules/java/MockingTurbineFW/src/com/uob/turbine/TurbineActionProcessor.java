package com.uob.turbine;

import org.apache.torque.criteria.Criteria;

public class TurbineActionProcessor {
    public String fetchData(String columnName, String value) {
        Criteria criteria = new Criteria();
        criteria.where(columnName, value);

        // Simulate fetching data from the database
        return "Fetched Data for " + value;
    }
}

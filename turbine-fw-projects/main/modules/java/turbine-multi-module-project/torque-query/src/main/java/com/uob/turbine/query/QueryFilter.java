package com.uob.turbine.query;

public class QueryFilter {
    private String field;
    private FilterOperator operator;
    private Object value;

    public QueryFilter(String field, FilterOperator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
}
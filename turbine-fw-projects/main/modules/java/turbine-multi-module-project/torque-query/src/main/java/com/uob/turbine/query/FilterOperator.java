package com.uob.turbine.query;

import org.apache.torque.criteria.SqlEnum;

public enum FilterOperator {
    EQUALS(SqlEnum.EQUAL),
    NOT_EQUALS(SqlEnum.NOT_EQUAL),
    GREATER_THAN(SqlEnum.GREATER_THAN),
    LESS_THAN(SqlEnum.LESS_THAN),
    LIKE(SqlEnum.LIKE),
    IN(SqlEnum.IN);

    private final SqlEnum sqlEnum;

    FilterOperator(SqlEnum sqlEnum) {
        this.sqlEnum = sqlEnum;
    }

    public SqlEnum getSqlEnum() {
        return sqlEnum;
    }
}
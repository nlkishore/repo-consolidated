package com.uob.portal.torque7.metadata;

/** JDBC column metadata snapshot (Torque 7 replacement for MapBuilder-driven column discovery). */
public final class ColumnMetadata {

    private final String name;
    private final int sqlType;
    private final String typeName;
    private final int columnSize;
    private final int nullable;

    public ColumnMetadata(String name, int sqlType, String typeName, int columnSize, int nullable) {
        this.name = name;
        this.sqlType = sqlType;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public int getSqlType() {
        return sqlType;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public int getNullable() {
        return nullable;
    }
}

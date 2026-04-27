package com.uob.portal.torque7.metadata;

import java.util.Collections;
import java.util.List;

/** Table plus ordered column metadata (legacy MapBuilder-style information for UI or tooling). */
public final class TableMetadata {

    private final String catalog;
    private final String schema;
    private final String tableName;
    private final List<ColumnMetadata> columns;

    public TableMetadata(String catalog, String schema, String tableName, List<ColumnMetadata> columns) {
        this.catalog = catalog;
        this.schema = schema;
        this.tableName = tableName;
        this.columns = Collections.unmodifiableList(columns);
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnMetadata> getColumns() {
        return columns;
    }
}

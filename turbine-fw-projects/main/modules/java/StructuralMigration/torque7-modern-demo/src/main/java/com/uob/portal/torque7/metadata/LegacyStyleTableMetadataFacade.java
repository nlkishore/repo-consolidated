package com.uob.portal.torque7.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;

/**
 * Replaces Torque 3 {@code MapBuilder} metadata discovery with JDBC {@link DatabaseMetaData} so
 * callers can still retrieve schema/table/column names during migration.
 */
public final class LegacyStyleTableMetadataFacade {

    private static final String DATABASE_NAME = "uob_portal";

    private LegacyStyleTableMetadataFacade() {}

    public static TableMetadata inspectTable(String tableName) throws TorqueException, SQLException {
        Connection con = Torque.getConnection(DATABASE_NAME);
        try {
            DatabaseMetaData md = con.getMetaData();
            String catalog = con.getCatalog();
            String schema = con.getSchema();
            List<ColumnMetadata> columns = new ArrayList<>();
            try (ResultSet rs = md.getColumns(catalog, schema, tableName, null)) {
                while (rs.next()) {
                    columns.add(
                            new ColumnMetadata(
                                    rs.getString("COLUMN_NAME"),
                                    rs.getInt("DATA_TYPE"),
                                    rs.getString("TYPE_NAME"),
                                    rs.getInt("COLUMN_SIZE"),
                                    rs.getInt("NULLABLE")));
                }
            }
            return new TableMetadata(catalog, schema, tableName, columns);
        } finally {
            Torque.closeConnection(con);
        }
    }
}

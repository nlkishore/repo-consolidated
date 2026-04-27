package com.uob.portal.torque3.map;

import java.sql.Types;

import org.apache.torque.Torque;
import org.apache.torque.map.DatabaseMap;
import org.apache.torque.map.MapBuilder;
import org.apache.torque.map.TableMap;

/**
 * Minimal {@link MapBuilder} for {@code gtp_user}, mirroring the legacy pattern where generated
 * MapBuilder classes registered table and column metadata with Torque.
 */
public final class GtpUserMapBuilder implements MapBuilder {

    private static final String DATABASE_NAME = "uob_portal";
    private static final String TABLE_NAME = "gtp_user";

    private DatabaseMap databaseMap;
    private boolean built;

    @Override
    public void doBuild() throws Exception {
        if (built) {
            return;
        }
        databaseMap = Torque.getDatabaseMap(DATABASE_NAME);
        if (!databaseMap.containsTable(TABLE_NAME)) {
            databaseMap.addTable(TABLE_NAME);
        }
        TableMap table = databaseMap.getTable(TABLE_NAME);
        table.setJavaName("GtpUser");
        table.setPrimaryKeyMethod("native");

        table.addPrimaryKey("user_id", Integer.valueOf(0), Types.INTEGER);
        table.addColumn("turbine_user_id", null, Types.INTEGER);
        table.addColumn("login_name", null, Types.VARCHAR, 255);
        table.addColumn("password_value", null, Types.VARCHAR, 255);
        table.addColumn("first_name", null, Types.VARCHAR, 255);
        table.addColumn("last_name", null, Types.VARCHAR, 255);
        table.addColumn("email", null, Types.VARCHAR, 255);

        built = true;
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

    @Override
    public DatabaseMap getDatabaseMap() {
        return databaseMap;
    }
}

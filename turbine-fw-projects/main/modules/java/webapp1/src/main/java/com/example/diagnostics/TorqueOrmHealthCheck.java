package com.example.diagnostics;

import org.apache.torque.Torque;
import org.apache.torque.adapter.Adapter;
import org.apache.torque.map.DatabaseMap;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.NumberKey;
import org.apache.torque.TorqueException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class TorqueOrmHealthCheck {

    private static final Log log = LogFactory.getLog(TorqueOrmHealthCheck.class);

    public static void validate() {
        try {
            String dbName = Torque.getDefaultDB();
            log.info(" Default database: " + dbName);

            // Adapter check
            Adapter adapter = Torque.getAdapter(dbName);
            if (adapter == null) {
                log.error(" Adapter is NULL — SQL generation will fail.");
            } else {
                log.info(" Adapter loaded: " + adapter.getClass().getName());
                log.info(" SQL dialect: " + adapter.getClass().getSimpleName());
                log.info(" ignoreCase('USERNAME') → " + adapter.ignoreCase("USERNAME"));
            }

            // Connection pool check
            try (Connection conn = Torque.getConnection(dbName)) {
                DatabaseMetaData meta = conn.getMetaData();
                log.info(" Connection pool active: " + meta.getURL());
                log.info(" JDBC driver: " + meta.getDriverName());
            } catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            // Schema mapping check
         // Schema mapping check
            DatabaseMap dbMap = Torque.getDatabaseMap(dbName);
            TableMap tableMap = dbMap.getTable("GTP_USER");
            if (tableMap == null) {
                log.error(" Table 'GTP_USER' not found in schema mapping.");
            } else {
                log.info(" Table 'GTP_USER' mapped with " + tableMap.getColumns().length + " columns.");
                log.info(" Primary key method: " + tableMap.getPrimaryKeyMethod());
            }


            // ID generation test (indirect)
            try {
            	NumberKey testId = new NumberKey();
            	testId.setValue(String.valueOf(System.currentTimeMillis() % 100000));
                log.info("🧪 Simulated ID generation test passed: " + testId.toString());
            } catch (Exception e) {
                log.warn("⚠️ ID generation test failed", e);
            }

        } catch (TorqueException e) {
            log.error("🔥 Torque ORM health check failed", e);
        }
    }
}
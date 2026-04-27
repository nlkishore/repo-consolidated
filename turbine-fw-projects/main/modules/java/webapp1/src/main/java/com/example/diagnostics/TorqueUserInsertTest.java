package com.example.diagnostics;

import com.example.om.GtpUser;
import org.apache.torque.Torque;
import org.apache.torque.util.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;

public class TorqueUserInsertTest {

    private static final Log log = LogFactory.getLog(TorqueUserInsertTest.class);

    public static void run() {
        Connection conn = null;
        try {
            String dbName = Torque.getDefaultDB();
            conn = Transaction.begin();

            // Create dummy user
            GtpUser user = new GtpUser();
            user.setFirstName("TorqueTest");
            user.setLastName("Rollback");
            user.setEmail("torque.test@example.com");
            user.setPassword("dummy123");
            user.setCreateDate(new java.util.Date());
            user.setLastLogin(new java.util.Date());

            // Insert user
            user.save(conn);
            log.info(" Dummy GTP_USER inserted with ID: " + user.getPrimaryKey());

            // Rollback to avoid polluting DB
            Transaction.rollback(conn);
            log.info(" Transaction rolled back successfully — no data persisted.");

        } catch (Exception e) {
            log.error(" Torque insert test failed", e);
            try {
                if (conn != null) {
                    Transaction.rollback(conn);
                    log.warn(" Rolled back due to error.");
                }
            } catch (Exception te) {
                log.error(" Rollback failed", te);
            }
        }
    }
}
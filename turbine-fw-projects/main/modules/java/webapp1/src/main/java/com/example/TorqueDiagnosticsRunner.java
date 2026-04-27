package com.example;

import org.apache.torque.Torque;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.example.diagnostics.TorqueOrmHealthCheck;
import com.example.diagnostics.TorqueUserInsertTest;

/**
 * Main class to run Torque diagnostics.
 */
public class TorqueDiagnosticsRunner {

    private static final Log log = LogFactory.getLog(TorqueDiagnosticsRunner.class);

    public static void main(String[] args) {
        try {
            log.info(" Starting Torque diagnostics...");

            // Initialize Torque
            Torque.init("Torque.properties");
            log.info(" Torque initialized successfully.");

            // Run ORM health check
            TorqueOrmHealthCheck.validate();

            // Run insert-and-rollback test
            TorqueUserInsertTest.run();

            log.info(" Diagnostics completed.");

        } catch (Exception e) {
            log.error(" Diagnostics failed", e);
        }
    }
}
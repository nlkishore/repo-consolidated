package com.example.diagnostics;

import org.apache.torque.Torque;
import org.apache.torque.adapter.Adapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validates that Torque adapter is loaded and logs SQL dialect.
 */
public class TorqueAdapterValidator {

    private static final Log log = LogFactory.getLog(TorqueAdapterValidator.class);

    public static void validate() {
        try {
            String defaultDb = Torque.getDefaultDB();
            log.info(" Torque default database: " + defaultDb);

            Adapter adapter = Torque.getAdapter(defaultDb);

            if (adapter == null) {
                log.error(" Torque adapter is NULL — SQL generation will fail.");
            } else {
                log.info(" Torque adapter loaded: " + adapter.getClass().getName());
                log.info(" SQL dialect: " + adapter.getClass().getSimpleName());
                log.info(" ignoreCase('USERNAME') → " + adapter.ignoreCase("USERNAME"));
            }
        } catch (Exception e) {
            log.error(" Torque adapter validation failed", e);
        }
    }
}
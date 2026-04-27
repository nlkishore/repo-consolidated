package com.uob.services.pull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.pull.TurbinePullService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * Custom PullService that safely handles null users.
 * This extends TurbinePullService to add null checks before accessing user methods.
 */
public class SafeTurbinePullService extends TurbinePullService {

    private static final Log log = LogFactory.getLog(SafeTurbinePullService.class);

    /**
     * Populate the context with user information, handling null users gracefully.
     * 
     * @param context the Velocity context to populate
     * @param data the PipelineData object (should be RunData)
     */
    @Override
    public void populateContext(Context context, PipelineData data) {
        if (!(data instanceof RunData)) {
            // If not RunData, delegate to parent
            super.populateContext(context, data);
            return;
        }
        
        RunData runData = (RunData) data;
        User user = null;
        
        try {
            // Safely get the user - may return null if not logged in
            user = runData.getUser();
        } catch (Exception e) {
            // If getUser() throws an exception, user remains null
            // This is acceptable for anonymous/unauthenticated users
            log.debug("Could not get user from RunData: " + e.getMessage());
        }
        
        // Check if user is null before calling methods on it
        if (user == null) {
            // For null users, try to populate context but catch NPEs
            try {
                super.populateContext(context, data);
            } catch (NullPointerException e) {
                // Expected for anonymous users - log at debug level
                log.debug("User is null (anonymous access), some context variables may not be available: " + e.getMessage());
                // Continue without populating user-specific context
            }
        } else {
            // User exists, safely call parent method
            try {
                super.populateContext(context, data);
            } catch (NullPointerException e) {
                // If super.populateContext still throws NPE, log and continue
                log.warn("Error populating context with user: " + e.getMessage(), e);
            }
        }
    }
}

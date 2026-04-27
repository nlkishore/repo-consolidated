package com.example.valves;

import org.apache.turbine.pipeline.Valve;
import org.apache.turbine.pipeline.ValveContext;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.apache.turbine.om.security.User;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logs the user state before layout rendering to help diagnose session injection issues.
 */
public class UserStateDebugValve implements Valve {

    private static final Log log = LogFactory.getLog(UserStateDebugValve.class);

    @Override
    public void invoke(PipelineData pipelineData, ValveContext context) {
        RunData data = (RunData) pipelineData;
        User user = data.getUser();

        if (user == null) {
            log.warn(" RunData.getUser() is NULL before layout rendering.");
        } else {
            log.info(" RunData.getUser() is present: " + user.getClass().getName());
            log.info(" hasLoggedIn: " + user.hasLoggedIn());
            log.info(" Name: " + user.getName());
            log.info(" Email: " + user.getEmail());
        }

        // Continue with the pipeline
        try {
			context.invokeNext(pipelineData);
		} catch (IOException | TurbineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
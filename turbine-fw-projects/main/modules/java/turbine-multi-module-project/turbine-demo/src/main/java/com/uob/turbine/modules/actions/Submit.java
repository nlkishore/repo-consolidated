package com.uob.turbine.modules.actions;

import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

public class Submit extends VelocityAction {
    @Override
    public void doPerform(PipelineData pipelineData, Context context) throws Exception {
    	RunData data = (RunData)pipelineData;
        String input = data.getParameters().getString("inputText");
        System.out.println("Received from form: " + input); // logs to server console
        data.setMessage("You submitted: " + input); // available in Velocity as $msg
    }
}

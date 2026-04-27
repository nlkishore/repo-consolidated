package org.example.turbine.actions;


import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

public class HelloAction extends VelocityAction {
    @Override
    public void doPerform(PipelineData pipelineData, Context context) throws Exception {
    	RunData data = (RunData)pipelineData;
        String name = data.getParameters().getString("name", "World");
        context.put("name", name);
        System.out.println("Received name: " + name); // server log
    }
}

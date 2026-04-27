package org.example.modules.screens;

import org.apache.turbine.modules.screens.VelocityScreen;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.TurbineServices;
import org.apache.velocity.context.Context;
import org.example.services.HelloService;

public class HelloScreen extends VelocityScreen {
	@Override
	protected void doBuildTemplate(PipelineData pipelineData, Context context) throws Exception {
		HelloService helloService = (HelloService) TurbineServices.getInstance()
				.getService(HelloService.class.getName());
		String name = null;
		try {
			name = helloService.getName(pipelineData); // ✅ This is correct

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		context.put("name", name);
	}
}

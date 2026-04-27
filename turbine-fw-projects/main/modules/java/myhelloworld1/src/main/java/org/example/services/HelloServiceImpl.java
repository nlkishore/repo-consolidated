package org.example.services;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.BaseService;

public class HelloServiceImpl extends BaseService implements HelloService {
	  @Override
	  public String getName(PipelineData data) {
	    return data.getRunData().getParameters().getString("name", "World");
}
}

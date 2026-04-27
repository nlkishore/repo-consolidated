package org.example.services;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.Service;

public interface HelloService extends Service {
	  String getName(PipelineData data);

	
	}

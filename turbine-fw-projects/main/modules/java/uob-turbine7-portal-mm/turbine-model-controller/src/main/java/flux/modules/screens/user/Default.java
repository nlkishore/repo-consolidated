package com.uob.flux.modules.screens.user;

/*
 * Copyright 2001-2019 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.uob.flux.modules.screens.FluxScreen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * Default screen for user management (FluxUserForm.vm)
 */
public class Default extends FluxScreen
{
	private static final Log log = LogFactory.getLog(Default.class);
	
	@Override
	protected void doBuildTemplate(PipelineData data, Context context) throws Exception {
		log.info("========================================");
		log.info("user.Default.doBuildTemplate() CALLED!");
		log.info("========================================");
		RunData runData = (RunData) data;
		log.info("user.Default - Screen template: " + runData.getScreenTemplate());
		log.info("user.Default - Screen: " + runData.getScreen());
		super.doBuildTemplate(data, context);
	}
}

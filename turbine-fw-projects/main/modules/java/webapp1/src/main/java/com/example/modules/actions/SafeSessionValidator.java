package com.example.modules.actions;

import com.example.om.GtpUser;
import com.example.wrapper.GtpUserWrapper;
import org.apache.turbine.util.RunData;
import org.apache.turbine.modules.actions.sessionvalidator.TemplateSessionValidator;
import org.apache.turbine.pipeline.PipelineData;

public class SafeSessionValidator extends TemplateSessionValidator {
    @Override
    public void doPerform(PipelineData pipelineData) throws Exception {
    	 RunData data = pipelineData.getRunData();
        if (data.getUser() == null) {
            GtpUser guest = new GtpUser();
            GtpUserWrapper wrappedGuest = new GtpUserWrapper(guest);
            wrappedGuest.setHasLoggedIn(false);
            data.setUser(wrappedGuest);
        }
        super.doPerform(data);
    }
}
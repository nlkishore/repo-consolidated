package com.example.screens;

import org.apache.turbine.modules.screens.VelocityScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.fulcrum.security.SecurityService;
import org.apache.fulcrum.security.AccessController;
import org.apache.fulcrum.security.PermissionManager;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.User;
import org.apache.turbine.services.TurbineServices;

public class PaymentScreen extends VelocityScreen {

    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        SecurityService securityService = (SecurityService) TurbineServices.getInstance().getService("SecurityService");

        AccessController accessController = securityService.getAccessController();
        PermissionManager permissionManager = securityService.getPermissionManager();

        User user = (User) data.getUser();
        Permission permission = permissionManager.getPermissionByName("PAYMENT_ACCESS");

        if (permission != null && accessController.hasPermission(user, permission)) {
            context.put("username", user.getName());
            setTemplate(data, "payment.vm");
        } else {
            setTemplate(data, "error.vm");
        }
    }
}
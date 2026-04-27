package com.example.screens;

import org.apache.fulcrum.security.SecurityService;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.UserManager;
import org.apache.fulcrum.security.RoleManager;
import org.apache.fulcrum.security.PermissionManager;
import org.apache.fulcrum.security.GroupManager;
import org.apache.turbine.services.TurbineServices;

public class InitSecuritySetup {

    public static void initialize() throws Exception {
        SecurityService securityService = (SecurityService) TurbineServices.getInstance().getService("SecurityService");

        UserManager userManager = securityService.getUserManager();
        RoleManager roleManager = securityService.getRoleManager();
        PermissionManager permissionManager = securityService.getPermissionManager();
        GroupManager groupManager = securityService.getGroupManager();

        // Create permissions
        Permission paymentPermission = permissionManager.getPermissionByName("PAYMENT_ACCESS");
        if (paymentPermission == null) {
            paymentPermission = permissionManager.addPermission(permissionManager.createPermission("PAYMENT_ACCESS"));
        }

        Permission tradePermission = permissionManager.getPermissionByName("TRADE_ACCESS");
        if (tradePermission == null) {
            tradePermission = permissionManager.addPermission(permissionManager.createPermission("TRADE_ACCESS"));
        }

        // Create roles
        Role paymentRole = roleManager.getRoleByName("PaymentUser");
        if (paymentRole == null) {
            paymentRole = roleManager.addRole(roleManager.createRole("PaymentUser"));
            roleManager.addPermission(paymentRole, paymentPermission);
        }

        Role tradeRole = roleManager.getRoleByName("TradeUser");
        if (tradeRole == null) {
            tradeRole = roleManager.addRole(roleManager.createRole("TradeUser"));
            roleManager.addPermission(tradeRole, tradePermission);
        }

        // Create group
        Group group = groupManager.getGroupByName("FunctionalGroup");
        if (group == null) {
            group = groupManager.addGroup(groupManager.createGroup("FunctionalGroup"));
        }

        // Create users and assign roles
        User userPayment = userManager.getUserByName("user_payment");
        if (userPayment == null) {
            userPayment = userManager.addUser(userManager.createUser("user_payment", "pass123"));
            userManager.addUserToGroup(userPayment, group);
            userManager.grantRole(userPayment, paymentRole, group);
        }

        User userTrade = userManager.getUserByName("user_trade");
        if (userTrade == null) {
            userTrade = userManager.addUser(userManager.createUser("user_trade", "pass123"));
            userManager.addUserToGroup(userTrade, group);
            userManager.grantRole(userTrade, tradeRole, group);
        }
    }
}
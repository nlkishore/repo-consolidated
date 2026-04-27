package com.uob.portal.torque3.turbine;

import java.lang.reflect.Method;

/**
 * Reflective adapter from a Turbine 2.x user object to the local {@link TurbineUserContract}.
 *
 * <p>It avoids a hard compile dependency on old Turbine artifacts while still supporting canonical
 * Turbine 2 getters like {@code getUserId()}, {@code getName()} and {@code getPassword()}.
 */
public final class Turbine2UserAdapter implements TurbineUserContract {

    private final Object turbineUser;

    public Turbine2UserAdapter(Object turbineUser) {
        this.turbineUser = turbineUser;
    }

    @Override
    public Integer getTurbineUserId() {
        Object out = invoke("getUserId");
        if (out == null) {
            return null;
        }
        if (out instanceof Number) {
            return Integer.valueOf(((Number) out).intValue());
        }
        return Integer.valueOf(String.valueOf(out));
    }

    @Override
    public String getUserName() {
        return invokeString("getName");
    }

    @Override
    public String getPassword() {
        return invokeString("getPassword");
    }

    @Override
    public String getFirstName() {
        return invokeString("getFirstName");
    }

    @Override
    public String getLastName() {
        return invokeString("getLastName");
    }

    @Override
    public String getEmail() {
        return invokeString("getEmail");
    }

    private Object invoke(String methodName) {
        try {
            Method m = turbineUser.getClass().getMethod(methodName);
            return m.invoke(turbineUser);
        } catch (Exception e) {
            return null;
        }
    }

    private String invokeString(String methodName) {
        Object out = invoke(methodName);
        return out == null ? null : String.valueOf(out);
    }
}

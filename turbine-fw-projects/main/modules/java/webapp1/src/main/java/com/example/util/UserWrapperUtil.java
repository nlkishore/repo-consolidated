package com.example.util;

import com.example.om.GtpUser;
import com.example.wrapper.GtpUserWrapper;
import org.apache.turbine.om.security.User;

/**
 * Utility class to wrap GtpUser instances into GtpUserWrapper
 * for safe use in Turbine session and rendering contexts.
 */
public final class UserWrapperUtil {

    private UserWrapperUtil() {
        // Prevent instantiation
    }

    /**
     * Wraps a given User object into a GtpUserWrapper if it's a GtpUser.
     * If already wrapped, returns as-is.
     *
     * @param user the raw User object (from SecurityService or anonymous fallback)
     * @param hasLoggedIn whether the user is considered logged in
     * @return a GtpUserWrapper instance
     */
    public static GtpUserWrapper wrapUser(User user, boolean hasLoggedIn) {
        if (user instanceof GtpUserWrapper) {
            GtpUserWrapper wrapper = (GtpUserWrapper) user;
            wrapper.setHasLoggedIn(hasLoggedIn);
            return wrapper;
        }

        if (user instanceof GtpUser) {
            GtpUserWrapper wrapper = new GtpUserWrapper((GtpUser) user);
            wrapper.setHasLoggedIn(hasLoggedIn);
            return wrapper;
        }

        throw new IllegalArgumentException("User must be GtpUser or GtpUserWrapper: " + user.getClass().getName());
    }
}
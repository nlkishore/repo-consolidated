package com.uob.portal.torque3.turbine;

/**
 * Minimal user contract to bridge Turbine user semantics into the legacy {@code gtp_user} table.
 *
 * <p>This keeps the demo independent from a specific Turbine API package while modelling fields
 * that are commonly required by Turbine-era authentication/user flows.
 */
public interface TurbineUserContract {

    Integer getTurbineUserId();

    String getUserName();

    String getPassword();

    String getFirstName();

    String getLastName();

    String getEmail();
}

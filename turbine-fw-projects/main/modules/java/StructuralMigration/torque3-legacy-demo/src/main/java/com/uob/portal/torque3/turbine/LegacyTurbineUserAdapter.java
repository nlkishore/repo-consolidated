package com.uob.portal.torque3.turbine;

import com.uob.portal.torque3.dto.GtpUserRow;

/**
 * Adapter that exposes a {@link GtpUserRow} as a Turbine-like user contract.
 */
public final class LegacyTurbineUserAdapter implements TurbineUserContract {

    private final GtpUserRow row;

    public LegacyTurbineUserAdapter(GtpUserRow row) {
        this.row = row;
    }

    @Override
    public Integer getTurbineUserId() {
        return row.getTurbineUserId();
    }

    @Override
    public String getUserName() {
        return row.getLoginName();
    }

    @Override
    public String getPassword() {
        return row.getPasswordValue();
    }

    @Override
    public String getFirstName() {
        return row.getFirstName();
    }

    @Override
    public String getLastName() {
        return row.getLastName();
    }

    @Override
    public String getEmail() {
        return row.getEmail();
    }
}

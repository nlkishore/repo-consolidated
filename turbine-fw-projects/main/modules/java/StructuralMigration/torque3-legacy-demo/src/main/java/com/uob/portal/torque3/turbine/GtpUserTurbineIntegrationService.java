package com.uob.portal.torque3.turbine;

import java.sql.SQLException;

import org.apache.torque.TorqueException;

import com.uob.portal.torque3.GtpUserLegacyCrudService;
import com.uob.portal.torque3.dto.GtpUserRow;
import com.workingdogs.village.DataSetException;

/**
 * Service that maps Turbine-style user payloads to {@code gtp_user} CRUD operations.
 */
public final class GtpUserTurbineIntegrationService {

    private GtpUserTurbineIntegrationService() {}

    /**
     * Creates a {@code gtp_user} row from a Turbine user payload.
     */
    public static GtpUserRow createFromTurbineUser(TurbineUserContract user)
            throws TorqueException, SQLException, DataSetException {
        return GtpUserLegacyCrudService.insertWithTurbineUserId(
                user.getTurbineUserId(),
                user.getUserName(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }

    /**
     * Creates a {@code gtp_user} row from a Turbine 2.x user object.
     *
     * <p>Expected user object getters: {@code getUserId()}, {@code getName()}, {@code getPassword()}.
     * Optional getters: {@code getFirstName()}, {@code getLastName()}, {@code getEmail()}.
     */
    public static GtpUserRow createFromTurbine2User(Object turbine2User)
            throws TorqueException, SQLException, DataSetException {
        return createFromTurbineUser(new Turbine2UserAdapter(turbine2User));
    }

    /**
     * Resolves a Turbine user by {@code turbine_user_id}.
     */
    public static TurbineUserContract findTurbineUserById(int turbineUserId)
            throws TorqueException, SQLException, DataSetException {
        GtpUserRow row = GtpUserLegacyCrudService.findByTurbineUserId(turbineUserId);
        if (row == null) {
            return null;
        }
        return new LegacyTurbineUserAdapter(row);
    }

    /**
     * Resolves a Turbine user by login name.
     */
    public static TurbineUserContract findTurbineUserByLogin(String loginName)
            throws TorqueException, SQLException, DataSetException {
        GtpUserRow row = GtpUserLegacyCrudService.findByLoginName(loginName);
        if (row == null) {
            return null;
        }
        return new LegacyTurbineUserAdapter(row);
    }
}

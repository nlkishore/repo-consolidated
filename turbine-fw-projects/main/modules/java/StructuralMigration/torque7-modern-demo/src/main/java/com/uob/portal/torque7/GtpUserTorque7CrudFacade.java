package com.uob.portal.torque7;

import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.criteria.Criteria;

import com.uob.portal.torque7.om.GtpUser;
import com.uob.portal.torque7.om.GtpUserPeer;

/**
 * Functional equivalent of {@link com.uob.portal.torque3.GtpUserLegacyCrudService} using Torque 7
 * generated {@link GtpUser} / {@link GtpUserPeer} and {@link Criteria}.
 */
public final class GtpUserTorque7CrudFacade {

    private GtpUserTorque7CrudFacade() {}

    public static GtpUser insert(
            String loginName, String passwordValue, String firstName, String lastName, String email)
            throws TorqueException {
        GtpUser row = new GtpUser();
        row.setLoginName(loginName);
        row.setPasswordValue(passwordValue);
        row.setFirstName(firstName);
        row.setLastName(lastName);
        row.setEmail(email);
        row.save();
        return row;
    }

    public static List<GtpUser> findAll() throws TorqueException {
        return GtpUserPeer.doSelect(new Criteria());
    }

    public static GtpUser findByUserId(Integer userId) throws TorqueException {
        Criteria c = new Criteria();
        c.where(GtpUserPeer.USER_ID, userId);
        List<GtpUser> rows = GtpUserPeer.doSelect(c);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static GtpUser findByLoginName(String loginName) throws TorqueException {
        Criteria c = new Criteria();
        c.where(GtpUserPeer.LOGIN_NAME, loginName);
        List<GtpUser> rows = GtpUserPeer.doSelect(c);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static GtpUser updateEmail(Integer userId, String email) throws TorqueException {
        GtpUser row = findByUserId(userId);
        if (row == null) {
            return null;
        }
        row.setEmail(email);
        row.save();
        return row;
    }

    public static boolean deleteByUserId(Integer userId) throws TorqueException {
        GtpUser row = findByUserId(userId);
        if (row == null) {
            return false;
        }
        GtpUserPeer.doDelete(row);
        return true;
    }
}

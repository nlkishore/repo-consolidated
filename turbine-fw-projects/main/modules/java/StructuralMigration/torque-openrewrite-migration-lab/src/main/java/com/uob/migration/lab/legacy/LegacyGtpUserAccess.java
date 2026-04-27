package com.uob.migration.lab.legacy;

import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;

import com.workingdogs.village.Record;

/**
 * Intentionally small Torque 3.x-shaped sample for OpenRewrite inventory recipes.
 * Not used at runtime by the Torque 7 demo module.
 */
public final class LegacyGtpUserAccess {

    private LegacyGtpUserAccess() {}

    @SuppressWarnings("unchecked")
    public List<Record> loadAllUserIdsRaw() throws TorqueException {
        return BasePeer.executeQuery("SELECT user_id FROM gtp_user", "uob_portal");
    }

    /** Criteria import is referenced so the inventory recipe can flag legacy package usage. */
    @SuppressWarnings("unused")
    public Criteria unusedLegacyCriteriaExample() {
        return new Criteria();
    }
}

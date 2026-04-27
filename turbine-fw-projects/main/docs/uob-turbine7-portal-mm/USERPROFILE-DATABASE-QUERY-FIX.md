# UserProfile Database Query Fix

## Problem
The database shows roles and permissions, but the UserProfile page displays "No groups assigned" (0 Groups, 0 Roles, 0 Permissions).

## Root Cause
The `acl.getRoles(group)` method was not returning roles even though the data exists in the database. This is likely due to:
1. Type mismatch between `GtpGroup` objects and what the ACL expects
2. ACL not properly loading data from the database
3. Framework-level issue with group/role retrieval

## Solution
**Implemented direct database query using Torque ORM** to bypass the ACL method and query the `GTP_USER_GROUP_ROLE` table directly.

### Changes Made

1. **Added Direct Database Query for Groups:**
   - Query `GTP_USER_GROUP_ROLE` table using Torque ORM
   - Get unique group IDs for the user
   - Retrieve Group objects using `security.getGroupById()`

2. **Added Direct Database Query for Roles:**
   - Query `GTP_USER_GROUP_ROLE` table to get user's roles
   - Group by GROUP_ID and ROLE_ID
   - Retrieve Role objects using `security.getRoleById()`
   - Build the `groupRolesMap` directly from database results

3. **Fallback to ACL Method:**
   - If database query fails, fall back to the original ACL method
   - This ensures backward compatibility

### Code Changes

**File:** `turbine-model-controller/src/main/java/modules/screens/UserProfile.java`

**New Imports:**
```java
import com.uob.om.GtpUserGroupRole;
import com.uob.om.GtpUserGroupRolePeer;
import com.uob.om.GtpGroupPeer;
import com.uob.om.GtpRolePeer;
import org.apache.torque.criteria.Criteria;
```

**Key Changes:**
1. Direct query to `GTP_USER_GROUP_ROLE` table to get user's groups
2. Direct query to get user's roles grouped by group
3. Enhanced logging to show what's being retrieved from database
4. Fallback to ACL method if database query fails

### How It Works

1. **Get Groups:**
   ```java
   Criteria criteria = new Criteria();
   criteria.where(GtpUserGroupRolePeer.USER_ID, userId);
   criteria.addGroupByColumn(GtpUserGroupRolePeer.GROUP_ID);
   List<GtpUserGroupRole> userGroupRoles = GtpUserGroupRolePeer.doSelect(criteria);
   ```

2. **Get Roles:**
   ```java
   // Query all user-group-role records
   // Group by GROUP_ID and ROLE_ID
   // Retrieve Role objects and build groupRolesMap
   ```

3. **Get Permissions:**
   - Uses existing method: `security.getPermissions(role)`
   - Works with roles retrieved from database

## Benefits

1. **Direct Database Access:** Bypasses ACL layer that wasn't working
2. **Reliable:** Queries the actual database tables directly
3. **Fast:** Single query to get all user-group-role mappings
4. **Backward Compatible:** Falls back to ACL method if database query fails

## Verification

After deploying, check the logs for:
```
UserProfile: Direct DB query found <N> group-role assignments for user <username>
UserProfile: ✓ Found group via DB query: <groupname> (ID: <id>)
UserProfile: ✓ Found role via DB: <rolename> in group: <groupname>
```

The profile page should now display:
- Groups the user belongs to
- Roles assigned to the user in each group
- Permissions associated with each role

## Next Steps

1. **Restart Tomcat** (or wait for auto-reload)
2. **Access UserProfile page** - should now show groups, roles, and permissions
3. **Check logs** - verify database queries are working

## Notes

- The direct database query approach is more reliable than relying on the ACL layer
- This solution works even if the ACL has issues with `GtpGroup` vs `TurbineGroup` type mismatches
- The code maintains backward compatibility by falling back to ACL method if needed

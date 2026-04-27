# UserProfile SQL Query Fix

## Problem
The database query was failing with SQL error:
```
Expression #3 of SELECT list is not in GROUP BY clause and contains nonaggregated column 'kishore.GTP_USER_GROUP_ROLE.ROLE_ID' which is not functionally dependent on columns in GROUP BY clause; this is incompatible with sql_mode=only_full_group_by
```

Additionally, the roles query was finding records but not displaying them because it was checking `if (groups.contains(group))` when groups was empty.

## Root Cause
1. **SQL GROUP BY Error:** Using `addGroupByColumn()` with Torque ORM was generating a SQL query that violated MySQL's `ONLY_FULL_GROUP_BY` mode
2. **Logic Error:** The roles query depended on groups being populated first, but since the groups query failed, roles weren't being added

## Solution

### Fix 1: Removed GROUP BY Clause
Changed from:
```java
criteria.addGroupByColumn(GtpUserGroupRolePeer.GROUP_ID);
```

To:
```java
// Don't use GROUP BY - just select all records and process in Java
```

This avoids the SQL error and we process unique group IDs in Java instead.

### Fix 2: Made Roles Query Independent
Changed the roles query to:
1. Add groups to the groups list if they're not already there
2. Not depend on groups being populated first
3. Process all user-group-role records and build the mapping

**Before:**
```java
if (group != null && groups.contains(group)) {
    // Only add roles if group is already in groups list
}
```

**After:**
```java
if (group != null) {
    // Add group to groups list if not already there
    if (!groups.contains(group)) {
        groups.add(group);
    }
    // Add roles regardless
}
```

## Changes Made

**File:** `turbine-model-controller/src/main/java/modules/screens/UserProfile.java`

1. **Removed GROUP BY from groups query** - processes unique group IDs in Java
2. **Made roles query add groups** - roles query now populates groups list if groups query failed
3. **Enhanced logging** - shows when groups and roles are found via database

## Expected Behavior

After this fix:
1. Database query should succeed without SQL errors
2. Groups should be found and added to the groups list
3. Roles should be found and mapped to groups
4. Permissions should be retrieved for each role
5. Profile page should display all data

## Verification

After restarting Tomcat, check logs for:
```
UserProfile: Direct DB query found <N> group-role assignments for user <username>
UserProfile: Found <N> unique groups for user <username>
UserProfile: ✓ Found group via DB query: <groupname> (ID: <id>)
UserProfile: Found <N> user-group-role records in database
UserProfile: ✓ Found role via DB: <rolename> (ID: <id>) in group: <groupname>
UserProfile: ✓ Mapped <N> role(s) to group: <groupname>
User profile loaded for: <username> - Groups: <N>, Roles: <M>, Permissions: <P>
```

## Next Steps

1. **Restart Tomcat** (or wait for auto-reload)
2. **Access UserProfile page** - should now display groups, roles, and permissions
3. **Check logs** - verify database queries are working and data is being retrieved

# UserProfile Not Showing Groups/Roles/Permissions - Fix

## Problem
The UserProfile page loads successfully, but shows **0 Groups, 0 Roles, 0 Permissions** for all users.

## Root Cause
**Users don't have group/role/permission assignments in the database.**

The logs show:
- `UserProfile: ACL retrieved: Yes` ✓
- `UserProfile: Total groups in system: 4` ✓
- `User profile loaded for: <user> - Groups: 0, Roles: 0, Permissions: 0` ✗

This indicates that:
1. The ACL (Access Control List) is being retrieved successfully
2. Groups exist in the system (4 groups found)
3. But users have **no group/role assignments** in the database

## Solution

### Step 1: Run the SQL Script
You need to run the SQL script to assign groups, roles, and permissions to all users:

**Script Location:** `webapp/src/main/data/assign-roles-to-all-users.sql`

**How to Run:**
1. **Using MySQL Workbench:**
   - Open MySQL Workbench
   - Connect to your database (default: `kishore`)
   - Open the SQL script file
   - Execute the script

2. **Using PowerShell:**
   ```powershell
   cd C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data
   .\assign-roles-to-all-users.ps1
   ```

3. **Using MySQL Command Line:**
   ```bash
   mysql -u root -p kishore < assign-roles-to-all-users.sql
   ```

### Step 2: What the Script Does
The script will:
1. Create groups: `ADMINISTRATORS`, `REGULAR_USERS`, `GUESTS`
2. Create roles: `ADMIN`, `MANAGER`, `USER`, `ANONYMOUS`
3. Create permissions and map them to roles
4. Map roles to groups
5. Assign roles to users based on their login name:
   - Users with 'admin' in name → ADMIN role
   - Users with 'manager' in name → MANAGER role
   - Other users → USER role
   - 'anon' user → ANONYMOUS role

### Step 3: Enhanced Logging
I've added enhanced logging to help diagnose the issue. After restarting Tomcat, check the logs for:

```
UserProfile: Checking <N> groups for user <username>
UserProfile: Checking group: <groupname> (ID: <id>)
UserProfile: acl.getRoles(<groupname>) returned: <result>
UserProfile: ✓ User <username> has roles in group: <groupname> (roles: <count>)
```

If you see:
```
UserProfile: ⚠ No groups found for user <username> - User may not have group/role assignments in database. Please run assign-roles-to-all-users.sql script.
```

This confirms the user needs group/role assignments.

## Verification

After running the SQL script and restarting Tomcat:

1. **Check the logs** - You should see:
   ```
   UserProfile: ✓ User <username> has roles in group: <groupname> (roles: <count>)
   User profile loaded for: <username> - Groups: <N>, Roles: <M>, Permissions: <P>
   ```

2. **Check the UserProfile page** - It should now display:
   - User groups with their associated roles
   - Roles with their associated permissions
   - Hierarchical view: Groups → Roles → Permissions

## Files Changed

1. **UserProfile.java** - Added enhanced logging to diagnose group/role retrieval:
   - Logs each group being checked
   - Logs the result of `acl.getRoles(group)` for each group
   - Warns if no groups found with instructions to run SQL script

## Next Steps

1. **Run the SQL script** (`assign-roles-to-all-users.sql`)
2. **Restart Tomcat** (or wait for auto-reload)
3. **Access UserProfile page** and verify groups/roles/permissions are displayed
4. **Check logs** if still not working - the enhanced logging will show exactly what's happening

## Notes

- The code logic is correct - it's checking all groups and using `acl.getRoles(group)` to find which groups the user belongs to
- The issue is purely that users don't have group/role assignments in the database
- Once the SQL script is run, the profile page should display all the data correctly

# Assign Roles, Groups, and Permissions to All Users

## Problem
Users are logging in but the UserProfile page shows no roles, groups, or permissions. This happens because users exist in the database but don't have group/role assignments.

## Solution
Run the SQL script `assign-roles-to-all-users.sql` to assign roles, groups, and permissions to ALL existing users in the database.

## Quick Start

### Option 1: Using PowerShell Script (Recommended)

```powershell
cd C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data
.\assign-roles-to-all-users.ps1
```

### Option 2: Using MySQL Command Line

```bash
cd C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data
mysql -u kishore -pKish1381@ kishore < assign-roles-to-all-users.sql
```

### Option 3: Using MySQL Workbench

1. Open MySQL Workbench
2. Connect to your MySQL server
3. Select the `kishore` database
4. Open `assign-roles-to-all-users.sql`
5. Execute the script

## What the Script Does

1. **Creates Groups** (if they don't exist):
   - ADMINISTRATORS
   - REGULAR_USERS
   - GUESTS

2. **Creates Roles** (if they don't exist):
   - ADMIN (all permissions)
   - MANAGER (VIEW_DASHBOARD, MANAGE_USERS)
   - USER (VIEW_DASHBOARD only)
   - ANONYMOUS (VIEW_DASHBOARD only)

3. **Creates Permissions** (if they don't exist):
   - VIEW_DASHBOARD
   - MANAGE_USERS
   - ADMIN_ACCESS

4. **Maps Roles to Permissions**:
   - ADMIN → all 3 permissions
   - MANAGER → VIEW_DASHBOARD, MANAGE_USERS
   - USER → VIEW_DASHBOARD
   - ANONYMOUS → VIEW_DASHBOARD

5. **Maps Groups to Roles**:
   - ADMINISTRATORS → ADMIN
   - REGULAR_USERS → MANAGER, USER
   - GUESTS → ANONYMOUS

6. **Assigns Roles to Users**:
   - Users with 'admin' in login_name → ADMINISTRATORS group, ADMIN role
   - Users with 'manager' in login_name → REGULAR_USERS group, MANAGER role
   - Users with 'anon' in login_name → GUESTS group, ANONYMOUS role
   - All other users → REGULAR_USERS group, USER role

## Verification

After running the script, verify the assignments:

```sql
-- View all users with their assignments
SELECT 
    u.LOGIN_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME,
    COUNT(DISTINCT p.PERMISSION_ID) as PERMISSION_COUNT
FROM GTP_USER u
LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
LEFT JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
LEFT JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
GROUP BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME
ORDER BY u.LOGIN_NAME;
```

## Testing the UserProfile Page

1. **Restart Tomcat** (if it's running) to ensure fresh database connections
2. **Login** to the application with any user account
3. **Navigate** to the UserProfile page:
   - URL: `http://localhost:8081/uob-t7-portal-mm-tomcat/app?screen=UserProfile`
   - Or click the UserProfile link in the navigation
4. **Verify** that you see:
   - User information (name, email, etc.)
   - Groups section with assigned groups
   - Roles section with assigned roles
   - Permissions section with permissions from roles
   - Hierarchical view showing Group → Role → Permission relationships

## Troubleshooting

### No data still showing after running script

1. **Check database connection**: Ensure the application is connecting to the correct database
2. **Check user exists**: Verify the user exists in `GTP_USER` table
3. **Check assignments**: Run the verification query above
4. **Check logs**: Look for errors in `avalon.log` or `application.log`
5. **Clear cache**: Restart Tomcat to clear any cached ACL data

### Users still don't have assignments

The script uses `INSERT IGNORE` to avoid duplicates. If a user already has an assignment, it won't be overwritten. To force reassignment:

1. Delete existing assignments:
   ```sql
   DELETE FROM GTP_USER_GROUP_ROLE;
   ```

2. Re-run the script

### Specific user needs different role

You can manually assign roles:

```sql
-- Example: Assign ADMIN role to a specific user
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
SELECT u.USER_ID, 1, 1  -- ADMINISTRATORS group, ADMIN role
FROM GTP_USER u
WHERE u.LOGIN_NAME = 'your_username';
```

## Files

- `assign-roles-to-all-users.sql` - SQL script to assign roles
- `assign-roles-to-all-users.ps1` - PowerShell wrapper script
- `gtp-test-data.sql` - Complete test data (users, roles, groups, permissions)
- `ASSIGN-ROLES-README.md` - This file

## Notes

- The script uses `INSERT IGNORE` to avoid errors if data already exists
- The script preserves existing assignments (won't overwrite)
- To reassign all users, delete from `GTP_USER_GROUP_ROLE` first
- The script assigns default roles based on login name patterns
- Anonymous user ('anon') is automatically assigned to GUESTS group with ANONYMOUS role

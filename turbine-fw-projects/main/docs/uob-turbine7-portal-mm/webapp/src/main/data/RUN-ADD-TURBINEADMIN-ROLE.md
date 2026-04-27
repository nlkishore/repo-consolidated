# How to Add turbineadmin Role for Flux Admin Access

## Problem
Flux admin screens (User, Group, Role, Permission management) require the `turbineadmin` role, but your database has `ADMIN` role. This causes "You do not have access to this part of the site" errors.

## Solution
Run the SQL script `add-turbineadmin-role.sql` to:
1. Add `turbineadmin` role to your database
2. Assign it to users who have `ADMIN` role
3. Grant all ADMIN permissions to `turbineadmin` role

## Option 1: MySQL Workbench (Recommended)

1. Open MySQL Workbench
2. Connect to your database (`kishore`)
3. Open the file: `webapp/src/main/data/add-turbineadmin-role.sql`
4. Execute the script (click the lightning bolt icon or press Ctrl+Shift+Enter)
5. Verify the results - you should see messages like "1 row(s) affected"

## Option 2: Command Line

```powershell
cd C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data
mysql -u root -p kishore < add-turbineadmin-role.sql
```

## Verification

After running the script, verify that your admin user has the `turbineadmin` role:

```sql
SELECT u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME
FROM GTP_USER u
INNER JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
INNER JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
INNER JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
WHERE u.LOGIN_NAME = 'admin'
  AND r.ROLE_NAME IN ('ADMIN', 'turbineadmin')
ORDER BY r.ROLE_NAME;
```

You should see both `ADMIN` and `turbineadmin` roles for the admin user.

## After Running the Script

1. **Logout** from the application
2. **Login again** as admin (this refreshes the user's ACL/roles)
3. Try accessing "Add New User", "Add New Role", or "Add New Permission" links
4. You should now have access!

## What the Script Does

- **Step 1**: Creates `turbineadmin` role (ID: 5) if it doesn't exist
- **Step 2**: Maps `turbineadmin` to `ADMINISTRATORS` group
- **Step 3**: Assigns `turbineadmin` role to all users who have `ADMIN` role
- **Step 4**: Grants all `ADMIN` permissions to `turbineadmin` role

This ensures full compatibility with Flux admin screens while preserving your existing `ADMIN` role setup.

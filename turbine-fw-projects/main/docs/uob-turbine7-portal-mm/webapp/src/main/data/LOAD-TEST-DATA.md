# Loading Test Data into GTP Tables

This guide explains how to load test data into the `kishore` database for the UOB Turbine 7 Portal application.

## Prerequisites

- MySQL 8.x server running
- Database `kishore` created
- User `kishore` with password `Kish1381@` has access to the database
- Tables created (run schema SQL first if not already done)

## Test Data Overview

The test data includes:
- **6 Users**: admin, manager1, manager2, user1, user2, **anon** (anonymous)
- **4 Roles**: ADMIN, MANAGER, USER, **ANONYMOUS**
- **3 Permissions**: VIEW_DASHBOARD, MANAGE_USERS, ADMIN_ACCESS
- **3 Groups**: ADMINISTRATORS, REGULAR_USERS, **GUESTS**

## Step 1: Create Database Schema (if not already done)

First, ensure the database schema exists:

```bash
# Navigate to the schema file location
cd webapp\src\main\data

# Execute schema creation
mysql -u kishore -pKish1381@ kishore < ..\..\..\torque-orm\target\generated-sql\torque\mysql\gtp-security-schema.sql
```

Or if the schema file is in a different location:

```bash
mysql -u kishore -pKish1381@ kishore < gtp-security-schema.sql
```

## Step 2: Load Test Data

Execute the test data SQL script:

### Option 1: Using MySQL Command Line

```bash
# Navigate to the data directory
cd webapp\src\main\data

# Execute the test data script
mysql -u kishore -pKish1381@ kishore < gtp-test-data.sql
```

### Option 2: Using MySQL Workbench or Other GUI

1. Open MySQL Workbench
2. Connect to your MySQL server
3. Select the `kishore` database
4. Open the file: `webapp\src\main\data\gtp-test-data.sql`
5. Execute the script

### Option 3: Using PowerShell

```powershell
cd webapp\src\main\data
$sql = Get-Content gtp-test-data.sql -Raw
mysql -u kishore -pKish1381@ kishore -e $sql
```

## Step 3: Verify Data

Run verification queries to confirm data was loaded:

```sql
-- Check users
SELECT USER_ID, LOGIN_NAME, FIRST_NAME, LAST_NAME, EMAIL FROM GTP_USER;

-- Check roles
SELECT * FROM GTP_ROLE;

-- Check permissions
SELECT * FROM GTP_PERMISSION;

-- Check groups
SELECT * FROM GTP_GROUP;

-- Check user-group-role mappings
SELECT 
    u.LOGIN_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME
FROM GTP_USER u
JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
ORDER BY u.LOGIN_NAME;
```

## Test User Credentials

After loading the data, you can login with these credentials:

| Username | Password | Role | Permissions |
|----------|----------|------|----------|
| **admin** | password123 | ADMIN | VIEW_DASHBOARD, MANAGE_USERS, ADMIN_ACCESS |
| **manager1** | password123 | MANAGER | VIEW_DASHBOARD, MANAGE_USERS |
| **manager2** | password123 | MANAGER | VIEW_DASHBOARD, MANAGE_USERS |
| **user1** | password123 | USER | VIEW_DASHBOARD |
| **user2** | password123 | USER | VIEW_DASHBOARD |
| **anon** | anon | ANONYMOUS | VIEW_DASHBOARD (for unauthenticated access) |

**Note:** The `anon` user is used by Turbine for anonymous/unauthenticated sessions. This user must exist in the database for the application to work properly with unauthenticated users.

## Access URLs

After deployment, access the application at:

- **Home Page**: `http://localhost:8081/uob-t7-portal-mm-tomcat/app`
- **Login Page**: `http://localhost:8081/uob-t7-portal-mm-tomcat/app?action=LoginUser`
- **User Profile**: `http://localhost:8081/uob-t7-portal-mm-tomcat/app?screen=UserProfile` (requires login)

## Troubleshooting

### Error: Table doesn't exist
- Ensure the schema SQL has been executed first
- Check that you're connected to the correct database (`kishore`)

### Error: Access denied
- Verify MySQL user credentials: `kishore` / `Kish1381@`
- Ensure the user has INSERT, UPDATE, DELETE permissions

### Error: Foreign key constraint fails
- Ensure data is inserted in the correct order (permissions → roles → groups → users → mappings)
- The script handles this automatically with `SET FOREIGN_KEY_CHECKS = 0`

### Password not working
- If using ClearCrypt (default), passwords are stored as plain text: `password123`
- If using JavaCrypt/MD5, update passwords to: `482c811da5d5b4bc6d497ffa98491e38`
- Check `componentConfiguration.xml` for the crypto algorithm setting

### Anonymous user not working
- Ensure the `anon` user exists in the database
- The anonymous user should be mapped to the GUESTS group and ANONYMOUS role
- Turbine uses `security.getAnonymousUser()` which expects a user with login name `anon`

## Notes

- **Password Security**: The test data uses plain text passwords for simplicity. In production, use proper password hashing.
- **Data Cleanup**: The script deletes existing data before inserting. Comment out the DELETE statements if you want to preserve existing data.
- **AUTO_INCREMENT**: The script resets AUTO_INCREMENT counters to ensure consistent IDs.

## Quick Load Command

For quick execution, use this single command:

```bash
mysql -u kishore -pKish1381@ kishore < webapp\src\main\data\gtp-test-data.sql
```

---

*Last Updated: 2026-01-24*

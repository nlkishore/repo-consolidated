# Quick Start: Assign Roles to All Users

## Using MySQL Workbench (Easiest Method)

Since you have MySQL Workbench installed at:
`C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe`

### Steps:

1. **Open MySQL Workbench**
   - Double-click: `C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe`
   - Or search for "MySQL Workbench" in Windows Start menu

2. **Connect to Database**
   - Click on your MySQL connection (or create new one)
   - **Host**: `localhost`
   - **Port**: `3306`
   - **Username**: `kishore`
   - **Password**: `Kish1381@`
   - **Default Schema**: `kishore`
   - Click **OK**

3. **Open SQL Script**
   - Go to **File** → **Open SQL Script**
   - Navigate to: `C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data\`
   - Select: `assign-roles-to-all-users.sql`
   - Click **Open**

4. **Execute Script**
   - Make sure `kishore` database is selected (check left panel)
   - Click the **Execute** button (⚡ lightning icon) or press `Ctrl+Shift+Enter`
   - Wait for "Script executed successfully" message

5. **Verify Results**
   - In a new query tab, paste and run:
   ```sql
   SELECT 
       u.LOGIN_NAME,
       g.GROUP_NAME,
       r.ROLE_NAME,
       COUNT(DISTINCT p.PERMISSION_ID) as PERMISSIONS
   FROM GTP_USER u
   LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
   LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
   LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
   LEFT JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
   LEFT JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
   GROUP BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME
   ORDER BY u.LOGIN_NAME;
   ```

6. **Test in Application**
   - Restart Tomcat (if running)
   - Login to application
   - Navigate to UserProfile page
   - Verify roles, groups, and permissions are displayed

## What Gets Assigned

- **Users with 'admin' in name** → ADMINISTRATORS group, ADMIN role (all permissions)
- **Users with 'manager' in name** → REGULAR_USERS group, MANAGER role (VIEW_DASHBOARD, MANAGE_USERS)
- **All other users** → REGULAR_USERS group, USER role (VIEW_DASHBOARD only)
- **'anon' user** → GUESTS group, ANONYMOUS role (VIEW_DASHBOARD only)

## Files Location

All files are in: `C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data\`

- `assign-roles-to-all-users.sql` - SQL script to execute
- `RUN-SQL-IN-WORKBENCH.md` - Detailed Workbench instructions
- `ASSIGN-ROLES-README.md` - Full documentation

## Troubleshooting

**Script won't execute?**
- Make sure you're connected to the `kishore` database
- Check the Output panel for error messages
- Verify all tables exist (GTP_USER, GTP_GROUP, etc.)

**No data showing after script?**
- Check verification query results
- Restart Tomcat to clear cache
- Check application logs for errors

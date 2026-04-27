# Running SQL Script in MySQL Workbench

## Quick Guide

### Step 1: Open MySQL Workbench
- Navigate to: `C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe`
- Or search for "MySQL Workbench" in Windows Start menu

### Step 2: Connect to Database
1. Click on your MySQL connection (or create a new one if needed)
2. Enter connection details:
   - **Host**: `localhost` (or your MySQL server)
   - **Port**: `3306` (default)
   - **Username**: `kishore`
   - **Password**: `Kish1381@`
   - **Default Schema**: `kishore`

### Step 3: Open SQL Script
1. In MySQL Workbench, go to **File** → **Open SQL Script**
2. Navigate to: `C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data\`
3. Select: `assign-roles-to-all-users.sql`

### Step 4: Execute Script
1. Make sure the `kishore` database is selected in the schema panel (left side)
2. Click the **Execute** button (⚡ lightning bolt icon) or press `Ctrl+Shift+Enter`
3. Wait for execution to complete
4. Check the **Output** panel at the bottom for success messages

### Step 5: Verify Results
Run this query in a new query tab to verify assignments:

```sql
SELECT 
    u.LOGIN_NAME,
    u.FIRST_NAME,
    u.LAST_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME,
    COUNT(DISTINCT p.PERMISSION_ID) as PERMISSION_COUNT
FROM GTP_USER u
LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
LEFT JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
LEFT JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
GROUP BY u.LOGIN_NAME, u.FIRST_NAME, u.LAST_NAME, g.GROUP_NAME, r.ROLE_NAME
ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME;
```

## Alternative: Using MySQL Command Line

If you prefer command line, you need to find the MySQL `mysql.exe` executable. It's typically located at:

- `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`
- Or check: `C:\Program Files\MySQL\MySQL Server 8.x\bin\mysql.exe`

Then run:
```powershell
cd C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u kishore -pKish1381@ kishore < assign-roles-to-all-users.sql
```

## Troubleshooting

### Can't find MySQL Workbench
- Check if it's installed: `C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe`
- If not found, download from: https://dev.mysql.com/downloads/workbench/

### Connection Error
- Verify MySQL server is running
- Check username/password: `kishore` / `Kish1381@`
- Verify database `kishore` exists

### Script Execution Error
- Ensure you're connected to the correct database (`kishore`)
- Check for foreign key constraint errors (script handles this with `SET FOREIGN_KEY_CHECKS = 0`)
- Verify all tables exist (GTP_USER, GTP_GROUP, GTP_ROLE, etc.)

### No Results After Execution
- Check the Output panel for error messages
- Run the verification query above to see current state
- Check if users exist: `SELECT * FROM GTP_USER;`

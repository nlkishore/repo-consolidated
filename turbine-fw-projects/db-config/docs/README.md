# MySQL Schema Cleanup and Creation

This directory contains scripts to clean up MySQL schemas and create a fresh `kishore` schema.

## Files

1. **cleanup-and-create-kishore.bat** - Windows batch script to automate the process
2. **cleanup-and-create-schema.sql** - SQL script for manual execution
3. **cleanup-schemas.sql** - Comprehensive cleanup script (drops multiple schemas)

## Quick Start

### Option 1: Automated (Recommended)

Run the batch script:
```cmd
cleanup-and-create-kishore.bat
```

**Note:** Update the MySQL path and credentials in the batch file if needed.

### Option 2: Manual SQL Execution

1. Connect to MySQL:
```cmd
"C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe" -u root -p
```

2. Run the SQL script:
```sql
source cleanup-and-create-schema.sql
```

Or execute directly:
```cmd
"C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe" -u root -p < cleanup-and-create-schema.sql
```

## What the Scripts Do

1. **Show current databases** - Lists all existing schemas
2. **Drop kishore schema** - Removes the `kishore` database if it exists (all data will be deleted)
3. **Create fresh kishore schema** - Creates a new `kishore` database with:
   - Character set: `utf8mb4`
   - Collation: `utf8mb4_unicode_ci`
4. **Verify creation** - Confirms the schema was created successfully

## Configuration

### Update MySQL Path
If MySQL is installed in a different location, update the path in `cleanup-and-create-kishore.bat`:
```batch
set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe"
```

### Update Credentials
Update the username and password in `cleanup-and-create-kishore.bat`:
```batch
set MYSQL_USER=root
set MYSQL_PASSWORD=your_password
```

## Safety Notes

⚠️ **WARNING**: Dropping a database will permanently delete all data, tables, and structures within that schema. Make sure you have backups if needed.

## System Schemas

The scripts preserve system schemas:
- `information_schema` - MySQL metadata
- `mysql` - MySQL system database
- `performance_schema` - Performance monitoring
- `sys` - System database

## Troubleshooting

### MySQL Service Not Running
```cmd
net start mysql81
```

### Access Denied
- Check username and password
- Verify MySQL user has DROP and CREATE privileges
- Try resetting password using `C:\mysql\pwdReset.bat`

### Path Not Found
- Verify MySQL installation path
- Update `MYSQL_BIN` variable in the batch file

## Example Output

```
============================================
MySQL Schema Cleanup and Creation
============================================

Step 1: Showing current databases...
+--------------------+
| Database           |
+--------------------+
| information_schema |
| kishore            |
| mysql              |
| performance_schema |
| sys                |
+--------------------+

Step 2: Dropping kishore schema if it exists...

Step 3: Creating fresh kishore schema...

Step 4: Verifying creation...
+--------------------+
| Database (kishore) |
+--------------------+
| kishore            |
+--------------------+

Step 5: Showing database details...
+----------+------------------------------------------------------------------+
| Database | Create Database                                                  |
+----------+------------------------------------------------------------------+
| kishore  | CREATE DATABASE `kishore` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ |
+----------+------------------------------------------------------------------+

============================================
SUCCESS: Fresh kishore schema created!
============================================
```

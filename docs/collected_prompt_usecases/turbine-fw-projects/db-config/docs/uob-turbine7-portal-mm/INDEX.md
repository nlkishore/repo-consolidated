# Documentation Index

**Location:** `C:\mysql81\uob-turbine7-portal-mm`  
**Project:** UOB Turbine7 Portal MM Database Setup

---

## 📚 Documentation Files

### Getting Started

1. **[README.md](README.md)** ⭐ **START HERE**
   - Complete setup guide
   - Database schema overview
   - Step-by-step instructions
   - Troubleshooting guide
   - Default credentials

2. **[QUICK-START.md](QUICK-START.md)**
   - Fast setup instructions
   - Automated script usage
   - Manual setup steps
   - Quick reference

3. **[DATABASE-SCHEMA.md](DATABASE-SCHEMA.md)**
   - Detailed table structures
   - Entity relationship diagrams
   - Column descriptions
   - Common queries
   - Maintenance procedures

---

## 🔧 Setup Scripts

### SQL Scripts (Run in Order)

1. **[01-create-database-and-user.sql](01-create-database-and-user.sql)**
   - Creates `kishore` database
   - Creates `kishore` user
   - Grants privileges
   - **Run as:** MySQL root user

2. **[02-create-tables.sql](02-create-tables.sql)**
   - Creates all 7 tables
   - Sets up foreign keys
   - Creates indexes
   - **Run as:** kishore user

3. **[03-load-test-data.sql](03-load-test-data.sql)**
   - Inserts roles, permissions, groups
   - Inserts test users
   - Creates all mappings
   - **Run as:** kishore user

4. **[04-verify-setup.sql](04-verify-setup.sql)**
   - Verifies database setup
   - Counts records
   - Displays sample data
   - Checks relationships
   - **Run as:** kishore user

5. **[05-assign-roles-to-all-users.sql](05-assign-roles-to-all-users.sql)**
   - Assigns roles to existing users
   - Based on username patterns
   - Useful for updating data
   - **Run as:** kishore user

### Automation Scripts

6. **[setup-database.ps1](setup-database.ps1)**
   - Windows PowerShell script
   - Automates all setup steps
   - Interactive prompts
   - Progress feedback

7. **[setup-database.sh](setup-database.sh)**
   - Linux/Mac Bash script
   - Automates all setup steps
   - Interactive prompts
   - Progress feedback

---

## 📋 Quick Reference

### Setup Order

1. **Database & User:** `01-create-database-and-user.sql`
2. **Tables:** `02-create-tables.sql`
3. **Test Data:** `03-load-test-data.sql`
4. **Verify:** `04-verify-setup.sql`

### Database Configuration

- **Database:** `kishore`
- **User:** `kishore`
- **Password:** `Kish1381@`
- **Host:** `localhost`
- **Port:** `3306`

### Test Users

| Username | Password | Role |
|----------|----------|------|
| admin | password123 | ADMIN |
| manager1 | password123 | MANAGER |
| user1 | password123 | USER |

### Tables

1. `GTP_USER` - User accounts
2. `GTP_ROLE` - Security roles
3. `GTP_PERMISSION` - Security permissions
4. `GTP_GROUP` - User groups
5. `GTP_ROLE_PERMISSION` - Role-Permission mapping
6. `GTP_GROUP_ROLE` - Group-Role mapping
7. `GTP_USER_GROUP_ROLE` - User-Group-Role mapping

---

## 🎯 Common Tasks

### New Setup

```powershell
# Windows
.\setup-database.ps1

# Linux/Mac
./setup-database.sh
```

### Add New User and Assign Role

```sql
-- 1. Insert user
INSERT INTO GTP_USER (LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL)
VALUES ('newuser', 'password123', 'New', 'User', 'newuser@uob.com');

-- 2. Assign role (run 05-assign-roles-to-all-users.sql or manually)
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
SELECT USER_ID, 2, 3  -- REGULAR_USERS group, USER role
FROM GTP_USER
WHERE LOGIN_NAME = 'newuser';
```

### Reset Database

```sql
DROP DATABASE IF EXISTS kishore;
-- Then run all setup scripts again
```

### Backup Database

```bash
mysqldump -u kishore -pKish1381@ kishore > backup.sql
```

---

## 📖 Documentation Structure

```
C:\mysql81\uob-turbine7-portal-mm\
├── README.md                          # Main documentation
├── QUICK-START.md                     # Quick reference
├── DATABASE-SCHEMA.md                 # Schema details
├── INDEX.md                           # This file
├── 01-create-database-and-user.sql   # Database/user creation
├── 02-create-tables.sql               # Table creation
├── 03-load-test-data.sql              # Test data loading
├── 04-verify-setup.sql                # Setup verification
├── 05-assign-roles-to-all-users.sql   # Role assignment
├── setup-database.ps1                 # Windows automation
└── setup-database.sh                  # Linux/Mac automation
```

---

## 🔍 Finding Information

### Need to...

- **Set up database for first time?** → See [QUICK-START.md](QUICK-START.md)
- **Understand table structure?** → See [DATABASE-SCHEMA.md](DATABASE-SCHEMA.md)
- **Troubleshoot issues?** → See [README.md](README.md) Troubleshooting section
- **Run scripts manually?** → See [README.md](README.md) Setup Steps
- **Understand relationships?** → See [DATABASE-SCHEMA.md](DATABASE-SCHEMA.md) ER Diagram
- **Add new users?** → See [05-assign-roles-to-all-users.sql](05-assign-roles-to-all-users.sql)
- **Verify setup?** → See [04-verify-setup.sql](04-verify-setup.sql)

---

## 📞 Support

For issues:
1. Check [README.md](README.md) Troubleshooting section
2. Review script output for errors
3. Verify MySQL service is running
4. Check application logs

---

**Last Updated:** January 2026

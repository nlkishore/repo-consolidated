# Quick Start Guide - Database Setup

**For:** UOB Turbine7 Portal MM  
**Location:** `C:\mysql81\uob-turbine7-portal-mm`

---

## 🚀 Fastest Setup (Automated)

### Windows (PowerShell)

```powershell
cd C:\mysql81\uob-turbine7-portal-mm
.\setup-database.ps1
```

**What it does:**
- Prompts for MySQL root password
- Creates database and user
- Creates all tables
- Loads test data
- Verifies setup

### Linux/Mac (Bash)

```bash
cd /path/to/mysql81/uob-turbine7-portal-mm
chmod +x setup-database.sh
./setup-database.sh
```

---

## 📋 Manual Setup (Step by Step)

If you prefer manual setup or need to run scripts individually:

### Step 1: Create Database and User

```bash
mysql -u root -p < 01-create-database-and-user.sql
```

**What it creates:**
- Database: `kishore`
- User: `kishore` with password `Kish1381@`

### Step 2: Create Tables

```bash
mysql -u kishore -pKish1381@ kishore < 02-create-tables.sql
```

**What it creates:**
- 7 tables with proper structure
- Foreign key constraints
- Indexes for performance

### Step 3: Load Test Data

```bash
mysql -u kishore -pKish1381@ kishore < 03-load-test-data.sql
```

**What it loads:**
- 4 roles (ADMIN, MANAGER, USER, ANONYMOUS)
- 3 permissions (VIEW_DASHBOARD, MANAGE_USERS, ADMIN_ACCESS)
- 3 groups (ADMINISTRATORS, REGULAR_USERS, GUESTS)
- 6 test users
- All mappings and relationships

### Step 4: Verify Setup

```bash
mysql -u kishore -pKish1381@ kishore < 04-verify-setup.sql
```

**What it verifies:**
- All tables exist
- Record counts are correct
- Relationships are valid
- Anonymous user exists

---

## 🔑 Default Credentials

### Database

- **Database:** `kishore`
- **Username:** `kishore`
- **Password:** `Kish1381@`

### Application Test Users

| Username | Password | Role | Access Level |
|----------|----------|------|--------------|
| admin | password123 | ADMIN | Full access (all permissions) |
| manager1 | password123 | MANAGER | View dashboard, manage users |
| manager2 | password123 | MANAGER | View dashboard, manage users |
| user1 | password123 | USER | View dashboard only |
| user2 | password123 | USER | View dashboard only |
| anon | anon | ANONYMOUS | View dashboard only (unauthenticated) |

---

## ✅ Verification Checklist

After setup, verify:

- [ ] Database `kishore` exists
- [ ] User `kishore` can connect
- [ ] All 7 tables exist
- [ ] Test data loaded (6 users, 4 roles, 3 permissions, 3 groups)
- [ ] Can login with `admin` / `password123`
- [ ] Application connects to database

---

## 🔧 Troubleshooting

### MySQL Not Found

**Windows:**
```powershell
# Add MySQL to PATH or use full path
& "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe" -u root -p
```

**Linux/Mac:**
```bash
# Install MySQL or add to PATH
export PATH=$PATH:/usr/local/mysql/bin
```

### Access Denied

- Verify MySQL root password is correct
- Check if MySQL service is running
- Verify user `kishore` was created

### Tables Already Exist

```sql
-- Drop all tables and recreate
DROP DATABASE IF EXISTS kishore;
CREATE DATABASE kishore;
-- Then run setup scripts again
```

---

## 📚 Additional Scripts

### Assign Roles to All Users

If you add new users and need to assign roles:

```bash
mysql -u kishore -pKish1381@ kishore < 05-assign-roles-to-all-users.sql
```

**What it does:**
- Assigns roles based on username patterns
- `admin*` → ADMIN role
- `manager*` → MANAGER role
- `anon` → ANONYMOUS role
- Others → USER role

---

## 📖 Full Documentation

For detailed information, see:
- **README.md** - Complete setup guide
- **02-create-tables.sql** - Table structure details
- **03-load-test-data.sql** - Test data details

---

## 🆘 Need Help?

1. Check **README.md** for detailed documentation
2. Review **04-verify-setup.sql** output for issues
3. Check MySQL error logs
4. Verify application configuration in `Torque.properties`

---

**Last Updated:** January 2026

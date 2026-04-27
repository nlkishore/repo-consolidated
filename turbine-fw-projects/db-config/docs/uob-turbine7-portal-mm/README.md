# UOB Turbine7 Portal MM - Database Setup Guide

**Version:** 1.0  
**Date:** January 2026  
**Project:** UOB Turbine 7 Portal Multi-Module  
**Database:** MySQL 8.1

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start](#quick-start)
4. [Database Schema](#database-schema)
5. [Setup Steps](#setup-steps)
6. [Scripts Description](#scripts-description)
7. [Verification](#verification)
8. [Troubleshooting](#troubleshooting)
9. [Default Credentials](#default-credentials)

---

## Overview

This folder contains all database setup scripts and documentation for the UOB Turbine7 Portal Multi-Module project. The application uses a custom security schema with the following tables:

- **GTP_USER** - User accounts
- **GTP_ROLE** - Security roles
- **GTP_PERMISSION** - Security permissions
- **GTP_GROUP** - User groups
- **GTP_ROLE_PERMISSION** - Maps roles to permissions
- **GTP_GROUP_ROLE** - Maps groups to roles
- **GTP_USER_GROUP_ROLE** - Maps users to groups and roles

### Database Configuration

- **Database Name:** `kishore`
- **Database User:** `kishore`
- **Database Password:** `Kish1381@`
- **Host:** `localhost`
- **Port:** `3306`

---

## Prerequisites

Before setting up the database, ensure you have:

1. **MySQL 8.1** (or compatible version) installed and running
2. **MySQL root access** to create database and user
3. **MySQL Workbench** or **MySQL Command Line Client** installed
4. **PowerShell** (for Windows) or **Bash** (for Linux/Mac) for running scripts

### Verify MySQL Installation

```bash
# Check MySQL version
mysql --version

# Test MySQL connection
mysql -u root -p
```

---

## Quick Start

For a quick setup, follow these steps:

1. **Create Database and User:**
   ```bash
   mysql -u root -p < 01-create-database-and-user.sql
   ```

2. **Create Tables:**
   ```bash
   mysql -u kishore -pKish1381@ kishore < 02-create-tables.sql
   ```

3. **Load Test Data:**
   ```bash
   mysql -u kishore -pKish1381@ kishore < 03-load-test-data.sql
   ```

4. **Verify Setup:**
   ```bash
   mysql -u kishore -pKish1381@ kishore < 04-verify-setup.sql
   ```

**OR** use the automated setup script:

```powershell
# Windows PowerShell
.\setup-database.ps1

# Linux/Mac Bash
./setup-database.sh
```

---

## Database Schema

### Table Structure

#### 1. GTP_USER
Stores user account information.

| Column | Type | Description |
|--------|------|-------------|
| USER_ID | INT (PK, AUTO_INCREMENT) | Unique user identifier |
| LOGIN_NAME | VARCHAR(255) | Username for login |
| PASSWORD_VALUE | VARCHAR(255) | Password (plain text or hash) |
| FIRST_NAME | VARCHAR(255) | User's first name |
| LAST_NAME | VARCHAR(255) | User's last name |
| EMAIL | VARCHAR(255) | User's email address |
| CONFIRM_VALUE | VARCHAR(255) | Account confirmation status |
| CREATED | DATETIME | Account creation timestamp |
| MODIFIED_DATE | DATETIME | Last modification timestamp |
| LAST_LOGIN | DATETIME | Last login timestamp |
| OBJECTDATA | TEXT | Additional user data (JSON/XML) |

#### 2. GTP_ROLE
Stores security roles.

| Column | Type | Description |
|--------|------|-------------|
| ROLE_ID | INT (PK, AUTO_INCREMENT) | Unique role identifier |
| ROLE_NAME | VARCHAR(255) | Role name (e.g., ADMIN, MANAGER, USER) |

#### 3. GTP_PERMISSION
Stores security permissions.

| Column | Type | Description |
|--------|------|-------------|
| PERMISSION_ID | INT (PK, AUTO_INCREMENT) | Unique permission identifier |
| PERMISSION_NAME | VARCHAR(255) | Permission name (e.g., VIEW_DASHBOARD, MANAGE_USERS) |

#### 4. GTP_GROUP
Stores user groups.

| Column | Type | Description |
|--------|------|-------------|
| GROUP_ID | INT (PK, AUTO_INCREMENT) | Unique group identifier |
| GROUP_NAME | VARCHAR(255) | Group name (e.g., ADMINISTRATORS, REGULAR_USERS) |

#### 5. GTP_ROLE_PERMISSION
Maps roles to permissions (Many-to-Many relationship).

| Column | Type | Description |
|--------|------|-------------|
| ROLE_ID | INT (FK) | Reference to GTP_ROLE |
| PERMISSION_ID | INT (FK) | Reference to GTP_PERMISSION |

**Primary Key:** (ROLE_ID, PERMISSION_ID)

#### 6. GTP_GROUP_ROLE
Maps groups to roles (Many-to-Many relationship).

| Column | Type | Description |
|--------|------|-------------|
| GROUP_ID | INT (FK) | Reference to GTP_GROUP |
| ROLE_ID | INT (FK) | Reference to GTP_ROLE |

**Primary Key:** (GROUP_ID, ROLE_ID)

#### 7. GTP_USER_GROUP_ROLE
Maps users to groups and roles (Many-to-Many relationship).

| Column | Type | Description |
|--------|------|-------------|
| USER_ID | INT (FK) | Reference to GTP_USER |
| GROUP_ID | INT (FK) | Reference to GTP_GROUP |
| ROLE_ID | INT (FK) | Reference to GTP_ROLE |

**Primary Key:** (USER_ID, GROUP_ID, ROLE_ID)

### Entity Relationship Diagram

```
GTP_USER
    |
    | (1:N)
    |
GTP_USER_GROUP_ROLE (N:M)
    |
    | (N:1)        (N:1)
    |                |
GTP_GROUP         GTP_ROLE
    |                |
    | (N:M)          | (N:M)
    |                |
GTP_GROUP_ROLE   GTP_ROLE_PERMISSION
    |                |
    | (N:1)          | (N:1)
    |                |
GTP_ROLE         GTP_PERMISSION
```

---

## Setup Steps

### Step 1: Create Database and User

Run the database and user creation script:

```bash
mysql -u root -p < 01-create-database-and-user.sql
```

This script:
- Creates the `kishore` database if it doesn't exist
- Creates the `kishore` user with password `Kish1381@`
- Grants all privileges on the `kishore` database
- Flushes privileges

**Expected Output:**
```
Database 'kishore' created successfully
User 'kishore'@'localhost' created successfully
Privileges granted successfully
```

### Step 2: Create Tables

Run the table creation script:

```bash
mysql -u kishore -pKish1381@ kishore < 02-create-tables.sql
```

This script:
- Creates all 7 tables with proper structure
- Sets up foreign key constraints
- Creates indexes for performance
- Sets up AUTO_INCREMENT values

**Expected Output:**
```
Table 'GTP_USER' created successfully
Table 'GTP_ROLE' created successfully
Table 'GTP_PERMISSION' created successfully
Table 'GTP_GROUP' created successfully
Table 'GTP_ROLE_PERMISSION' created successfully
Table 'GTP_GROUP_ROLE' created successfully
Table 'GTP_USER_GROUP_ROLE' created successfully
```

### Step 3: Load Test Data

Run the test data script:

```bash
mysql -u kishore -pKish1381@ kishore < 03-load-test-data.sql
```

This script:
- Inserts 4 roles (ADMIN, MANAGER, USER, ANONYMOUS)
- Inserts 3 permissions (VIEW_DASHBOARD, MANAGE_USERS, ADMIN_ACCESS)
- Inserts 3 groups (ADMINISTRATORS, REGULAR_USERS, GUESTS)
- Maps roles to permissions
- Maps groups to roles
- Inserts 6 test users with default password
- Maps users to groups and roles

**Expected Output:**
```
4 roles inserted
3 permissions inserted
3 groups inserted
Role-permission mappings created
Group-role mappings created
6 users inserted
User-group-role mappings created
```

### Step 4: Verify Setup

Run the verification script:

```bash
mysql -u kishore -pKish1381@ kishore < 04-verify-setup.sql
```

This script:
- Counts records in each table
- Displays sample data
- Verifies relationships
- Checks for required data (e.g., anonymous user)

---

## Scripts Description

### 01-create-database-and-user.sql
Creates the database and user account.

**Usage:**
```bash
mysql -u root -p < 01-create-database-and-user.sql
```

**What it does:**
- Creates `kishore` database
- Creates `kishore` user with password `Kish1381@`
- Grants all privileges
- Flushes privileges

### 02-create-tables.sql
Creates all database tables with proper structure.

**Usage:**
```bash
mysql -u kishore -pKish1381@ kishore < 02-create-tables.sql
```

**What it does:**
- Creates 7 tables with proper columns and data types
- Sets up primary keys and foreign keys
- Creates indexes for performance
- Sets AUTO_INCREMENT starting values

### 03-load-test-data.sql
Loads test data for development and testing.

**Usage:**
```bash
mysql -u kishore -pKish1381@ kishore < 03-load-test-data.sql
```

**What it does:**
- Inserts roles, permissions, and groups
- Creates role-permission mappings
- Creates group-role mappings
- Inserts test users
- Creates user-group-role assignments

### 04-verify-setup.sql
Verifies the database setup is correct.

**Usage:**
```bash
mysql -u kishore -pKish1381@ kishore < 04-verify-setup.sql
```

**What it does:**
- Counts records in each table
- Displays sample data
- Verifies relationships
- Checks data integrity

### 05-assign-roles-to-all-users.sql
Assigns roles to all existing users (useful for updating existing data).

**Usage:**
```bash
mysql -u kishore -pKish1381@ kishore < 05-assign-roles-to-all-users.sql
```

**What it does:**
- Ensures all users have at least one group assignment
- Ensures all users have at least one role assignment
- Assigns roles based on username patterns (admin → ADMIN, manager → MANAGER, etc.)

### setup-database.ps1 (Windows)
Automated setup script for Windows PowerShell.

**Usage:**
```powershell
.\setup-database.ps1
```

**What it does:**
- Prompts for MySQL root password
- Runs all setup scripts in sequence
- Provides progress feedback
- Verifies setup at the end

### setup-database.sh (Linux/Mac)
Automated setup script for Linux/Mac Bash.

**Usage:**
```bash
chmod +x setup-database.sh
./setup-database.sh
```

**What it does:**
- Prompts for MySQL root password
- Runs all setup scripts in sequence
- Provides progress feedback
- Verifies setup at the end

---

## Verification

After setup, verify the database is configured correctly:

### 1. Check Database Exists

```sql
SHOW DATABASES LIKE 'kishore';
```

### 2. Check Tables Exist

```sql
USE kishore;
SHOW TABLES;
```

Expected output:
```
GTP_USER
GTP_ROLE
GTP_PERMISSION
GTP_GROUP
GTP_ROLE_PERMISSION
GTP_GROUP_ROLE
GTP_USER_GROUP_ROLE
```

### 3. Check User Count

```sql
SELECT COUNT(*) as total_users FROM GTP_USER;
```

Expected: At least 6 users (including anonymous user)

### 4. Check Role Count

```sql
SELECT COUNT(*) as total_roles FROM GTP_ROLE;
```

Expected: 4 roles

### 5. Check Complete User Access

```sql
SELECT 
    u.LOGIN_NAME,
    u.FIRST_NAME,
    u.LAST_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME,
    p.PERMISSION_NAME
FROM GTP_USER u
JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME, p.PERMISSION_NAME;
```

This query shows the complete access matrix for all users.

---

## Troubleshooting

### Issue: "Access denied for user 'kishore'@'localhost'"

**Solution:**
1. Verify the user was created:
   ```sql
   SELECT User, Host FROM mysql.user WHERE User='kishore';
   ```
2. If user doesn't exist, run `01-create-database-and-user.sql` again
3. If user exists, verify password is correct: `Kish1381@`

### Issue: "Table already exists"

**Solution:**
1. Drop existing tables:
   ```sql
   DROP TABLE IF EXISTS GTP_USER_GROUP_ROLE;
   DROP TABLE IF EXISTS GTP_GROUP_ROLE;
   DROP TABLE IF EXISTS GTP_ROLE_PERMISSION;
   DROP TABLE IF EXISTS GTP_USER;
   DROP TABLE IF EXISTS GTP_GROUP;
   DROP TABLE IF EXISTS GTP_ROLE;
   DROP TABLE IF EXISTS GTP_PERMISSION;
   ```
2. Run `02-create-tables.sql` again

### Issue: "Foreign key constraint fails"

**Solution:**
1. Ensure tables are created in the correct order (run `02-create-tables.sql`)
2. Ensure data is loaded in the correct order (run `03-load-test-data.sql`)
3. Check that referenced IDs exist before inserting relationships

### Issue: "Cannot connect to MySQL server"

**Solution:**
1. Verify MySQL service is running:
   ```bash
   # Windows
   net start MySQL80
   
   # Linux
   sudo systemctl start mysql
   ```
2. Verify MySQL is listening on port 3306
3. Check firewall settings

### Issue: "Unknown database 'kishore'"

**Solution:**
1. Run `01-create-database-and-user.sql` first
2. Verify database was created:
   ```sql
   SHOW DATABASES LIKE 'kishore';
   ```

---

## Default Credentials

### Database Credentials

- **Database:** `kishore`
- **Username:** `kishore`
- **Password:** `Kish1381@`
- **Host:** `localhost`
- **Port:** `3306`

### Application Test Users

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| admin | password123 | ADMIN | All permissions (VIEW_DASHBOARD, MANAGE_USERS, ADMIN_ACCESS) |
| manager1 | password123 | MANAGER | VIEW_DASHBOARD, MANAGE_USERS |
| manager2 | password123 | MANAGER | VIEW_DASHBOARD, MANAGE_USERS |
| user1 | password123 | USER | VIEW_DASHBOARD only |
| user2 | password123 | USER | VIEW_DASHBOARD only |
| anon | anon | ANONYMOUS | VIEW_DASHBOARD only (for unauthenticated access) |

**⚠️ IMPORTANT:** Change these passwords in production!

---

## Additional Resources

- **Project Documentation:** See `COMPREHENSIVE-TROUBLESHOOTING-GUIDE.md` in project root
- **Application Configuration:** `webapp/src/main/webapp/WEB-INF/conf/Torque.properties`
- **MySQL Documentation:** https://dev.mysql.com/doc/

---

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the application logs: `apache-tomcat-10.1.44/webapps/uob-t7-portal-mm-tomcat/logs/`
3. Verify database connection in `Torque.properties`

---

**Last Updated:** January 2026  
**Maintained By:** UOB Development Team

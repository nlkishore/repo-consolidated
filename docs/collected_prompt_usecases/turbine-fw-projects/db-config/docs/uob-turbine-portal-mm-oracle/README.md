# UOB Turbine7 Portal MM - Oracle Database Setup Guide

**Version:** 1.0  
**Date:** January 2026  
**Project:** UOB Turbine 7 Portal Multi-Module  
**Database:** Oracle Database 12c or later

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
10. [Oracle-Specific Notes](#oracle-specific-notes)

---

## Overview

This folder contains all Oracle database setup scripts and documentation for the UOB Turbine7 Portal Multi-Module project. The application uses a custom security schema with the following tables:

- **GTP_USER** - User accounts
- **GTP_ROLE** - Security roles
- **GTP_PERMISSION** - Security permissions
- **GTP_GROUP** - User groups
- **GTP_ROLE_PERMISSION** - Maps roles to permissions
- **GTP_GROUP_ROLE** - Maps groups to roles
- **GTP_USER_GROUP_ROLE** - Maps users to groups and roles

### Database Configuration

- **Schema/User:** `KISHORE`
- **Password:** `Kish1381@`
- **Tablespace:** `USERS` (default)
- **Host:** `localhost`
- **Port:** `1521` (default)
- **Service Name/SID:** `ORCL` (or your Oracle instance)

---

## Prerequisites

Before setting up the database, ensure you have:

1. **Oracle Database 12c or later** installed and running
2. **SYSDBA or DBA privileges** to create users and tablespaces
3. **SQL*Plus** or **SQL Developer** installed
4. **PowerShell** (for Windows) or **Bash** (for Linux/Mac) for running scripts

### Verify Oracle Installation

```bash
# Check Oracle version
sqlplus -version

# Test Oracle connection (as SYSDBA)
sqlplus sys/password@localhost:1521/ORCL as sysdba
```

---

## Quick Start

For a quick setup, follow these steps:

1. **Create Schema and User:**
   ```bash
   sqlplus sys/password@localhost:1521/ORCL as sysdba @01-create-schema-and-user.sql
   ```

2. **Create Tables:**
   ```bash
   sqlplus kishore/Kish1381@localhost:1521/ORCL @02-create-tables.sql
   ```

3. **Load Test Data:**
   ```bash
   sqlplus kishore/Kish1381@localhost:1521/ORCL @03-load-test-data.sql
   ```

4. **Verify Setup:**
   ```bash
   sqlplus kishore/Kish1381@localhost:1521/ORCL @04-verify-setup.sql
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
| USER_ID | NUMBER(10) (PK) | Unique user identifier (from sequence) |
| LOGIN_NAME | VARCHAR2(255) | Username for login |
| PASSWORD_VALUE | VARCHAR2(255) | Password (plain text or hash) |
| FIRST_NAME | VARCHAR2(255) | User's first name |
| LAST_NAME | VARCHAR2(255) | User's last name |
| EMAIL | VARCHAR2(255) | User's email address |
| CONFIRM_VALUE | VARCHAR2(255) | Account confirmation status |
| CREATED | TIMESTAMP | Account creation timestamp |
| MODIFIED_DATE | TIMESTAMP | Last modification timestamp |
| LAST_LOGIN | TIMESTAMP | Last login timestamp |
| OBJECTDATA | CLOB | Additional user data (JSON/XML) |

#### 2. GTP_ROLE
Stores security roles.

| Column | Type | Description |
|--------|------|-------------|
| ROLE_ID | NUMBER(10) (PK) | Unique role identifier (from sequence) |
| ROLE_NAME | VARCHAR2(255) | Role name (e.g., ADMIN, MANAGER, USER) |

#### 3. GTP_PERMISSION
Stores security permissions.

| Column | Type | Description |
|--------|------|-------------|
| PERMISSION_ID | NUMBER(10) (PK) | Unique permission identifier (from sequence) |
| PERMISSION_NAME | VARCHAR2(255) | Permission name (e.g., VIEW_DASHBOARD, MANAGE_USERS) |

#### 4. GTP_GROUP
Stores user groups.

| Column | Type | Description |
|--------|------|-------------|
| GROUP_ID | NUMBER(10) (PK) | Unique group identifier (from sequence) |
| GROUP_NAME | VARCHAR2(255) | Group name (e.g., ADMINISTRATORS, REGULAR_USERS) |

#### 5. GTP_ROLE_PERMISSION
Maps roles to permissions (Many-to-Many relationship).

| Column | Type | Description |
|--------|------|-------------|
| ROLE_ID | NUMBER(10) (FK) | Reference to GTP_ROLE |
| PERMISSION_ID | NUMBER(10) (FK) | Reference to GTP_PERMISSION |

**Primary Key:** (ROLE_ID, PERMISSION_ID)

#### 6. GTP_GROUP_ROLE
Maps groups to roles (Many-to-Many relationship).

| Column | Type | Description |
|--------|------|-------------|
| GROUP_ID | NUMBER(10) (FK) | Reference to GTP_GROUP |
| ROLE_ID | NUMBER(10) (FK) | Reference to GTP_ROLE |

**Primary Key:** (GROUP_ID, ROLE_ID)

#### 7. GTP_USER_GROUP_ROLE
Maps users to groups and roles (Many-to-Many relationship).

| Column | Type | Description |
|--------|------|-------------|
| USER_ID | NUMBER(10) (FK) | Reference to GTP_USER |
| GROUP_ID | NUMBER(10) (FK) | Reference to GTP_GROUP |
| ROLE_ID | NUMBER(10) (FK) | Reference to GTP_ROLE |

**Primary Key:** (USER_ID, GROUP_ID, ROLE_ID)

### Sequences

Oracle uses sequences instead of AUTO_INCREMENT:

- **SEQ_GTP_USER** - For GTP_USER.USER_ID
- **SEQ_GTP_ROLE** - For GTP_ROLE.ROLE_ID
- **SEQ_GTP_PERMISSION** - For GTP_PERMISSION.PERMISSION_ID
- **SEQ_GTP_GROUP** - For GTP_GROUP.GROUP_ID

---

## Setup Steps

### Step 1: Create Schema and User

Run the schema and user creation script:

```bash
sqlplus sys/password@localhost:1521/ORCL as sysdba @01-create-schema-and-user.sql
```

This script:
- Creates the `KISHORE` user/schema
- Sets password to `Kish1381@`
- Grants necessary privileges
- Creates default tablespace (if needed)

**Expected Output:**
```
User created.
Grant succeeded.
```

### Step 2: Create Tables

Run the table creation script:

```bash
sqlplus kishore/Kish1381@localhost:1521/ORCL @02-create-tables.sql
```

This script:
- Creates all 7 tables with proper structure
- Creates sequences for primary keys
- Sets up foreign key constraints
- Creates indexes for performance

**Expected Output:**
```
Table created.
Sequence created.
```

### Step 3: Load Test Data

Run the test data script:

```bash
sqlplus kishore/Kish1381@localhost:1521/ORCL @03-load-test-data.sql
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
1 row created.
```

### Step 4: Verify Setup

Run the verification script:

```bash
sqlplus kishore/Kish1381@localhost:1521/ORCL @04-verify-setup.sql
```

This script:
- Counts records in each table
- Displays sample data
- Verifies relationships
- Checks for required data (e.g., anonymous user)

---

## Scripts Description

### 01-create-schema-and-user.sql
Creates the schema and user account.

**Usage:**
```bash
sqlplus sys/password@localhost:1521/ORCL as sysdba @01-create-schema-and-user.sql
```

**What it does:**
- Creates `KISHORE` user/schema
- Sets password to `Kish1381@`
- Grants CONNECT, RESOURCE, CREATE SESSION privileges
- Creates default tablespace (if needed)

### 02-create-tables.sql
Creates all database tables with proper structure.

**Usage:**
```bash
sqlplus kishore/Kish1381@localhost:1521/ORCL @02-create-tables.sql
```

**What it does:**
- Creates 7 tables with proper columns and data types
- Creates 4 sequences for primary keys
- Sets up primary keys and foreign keys
- Creates indexes for performance

### 03-load-test-data.sql
Loads test data for development and testing.

**Usage:**
```bash
sqlplus kishore/Kish1381@localhost:1521/ORCL @03-load-test-data.sql
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
sqlplus kishore/Kish1381@localhost:1521/ORCL @04-verify-setup.sql
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
sqlplus kishore/Kish1381@localhost:1521/ORCL @05-assign-roles-to-all-users.sql
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
- Prompts for Oracle SYSDBA password
- Prompts for Oracle connection details
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
- Prompts for Oracle SYSDBA password
- Prompts for Oracle connection details
- Runs all setup scripts in sequence
- Provides progress feedback
- Verifies setup at the end

---

## Verification

After setup, verify the database is configured correctly:

### 1. Check User Exists

```sql
SELECT username FROM all_users WHERE username = 'KISHORE';
```

### 2. Check Tables Exist

```sql
SELECT table_name FROM user_tables ORDER BY table_name;
```

Expected output:
```
GTP_GROUP
GTP_PERMISSION
GTP_ROLE
GTP_ROLE_PERMISSION
GTP_GROUP_ROLE
GTP_USER
GTP_USER_GROUP_ROLE
```

### 3. Check Sequences Exist

```sql
SELECT sequence_name FROM user_sequences ORDER BY sequence_name;
```

Expected output:
```
SEQ_GTP_GROUP
SEQ_GTP_PERMISSION
SEQ_GTP_ROLE
SEQ_GTP_USER
```

### 4. Check User Count

```sql
SELECT COUNT(*) as total_users FROM GTP_USER;
```

Expected: At least 6 users (including anonymous user)

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

### Issue: "ORA-01017: invalid username/password"

**Solution:**
1. Verify the password is correct: `Kish1381@`
2. Check if user exists:
   ```sql
   SELECT username FROM all_users WHERE username = 'KISHORE';
   ```
3. If user doesn't exist, run `01-create-schema-and-user.sql` again

### Issue: "ORA-00955: name is already used by an existing object"

**Solution:**
1. Drop existing objects:
   ```sql
   DROP TABLE GTP_USER_GROUP_ROLE CASCADE CONSTRAINTS;
   DROP TABLE GTP_GROUP_ROLE CASCADE CONSTRAINTS;
   DROP TABLE GTP_ROLE_PERMISSION CASCADE CONSTRAINTS;
   DROP TABLE GTP_USER CASCADE CONSTRAINTS;
   DROP TABLE GTP_GROUP CASCADE CONSTRAINTS;
   DROP TABLE GTP_ROLE CASCADE CONSTRAINTS;
   DROP TABLE GTP_PERMISSION CASCADE CONSTRAINTS;
   DROP SEQUENCE SEQ_GTP_USER;
   DROP SEQUENCE SEQ_GTP_ROLE;
   DROP SEQUENCE SEQ_GTP_PERMISSION;
   DROP SEQUENCE SEQ_GTP_GROUP;
   ```
2. Run `02-create-tables.sql` again

### Issue: "ORA-12541: TNS:no listener"

**Solution:**
1. Verify Oracle listener is running:
   ```bash
   lsnrctl status
   ```
2. Start Oracle listener if needed:
   ```bash
   lsnrctl start
   ```
3. Verify Oracle service is running

### Issue: "ORA-01034: ORACLE not available"

**Solution:**
1. Verify Oracle database instance is running
2. Check Oracle service status
3. Connect as SYSDBA to verify:
   ```sql
   sqlplus sys/password as sysdba
   SELECT instance_name, status FROM v$instance;
   ```

### Issue: "ORA-01950: no privileges on tablespace"

**Solution:**
1. Grant tablespace quota to user:
   ```sql
   ALTER USER KISHORE QUOTA UNLIMITED ON USERS;
   ```

---

## Default Credentials

### Database Credentials

- **Schema/User:** `KISHORE`
- **Password:** `Kish1381@`
- **Host:** `localhost`
- **Port:** `1521`
- **Service Name/SID:** `ORCL` (or your Oracle instance)

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

## Oracle-Specific Notes

### Connection String Format

**TNS Format:**
```
(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=ORCL)))
```

**Easy Connect Format:**
```
localhost:1521/ORCL
```

### Application Configuration

Update `Torque.properties` to use Oracle:

```properties
torque.database.default=kishore
torque.database.kishore.adapter=oracle

torque.dsfactory.kishore.connection.driver = oracle.jdbc.OracleDriver
torque.dsfactory.kishore.connection.url = jdbc:oracle:thin:@localhost:1521:ORCL
torque.dsfactory.kishore.connection.user = KISHORE
torque.dsfactory.kishore.connection.password = Kish1381@
torque.dsfactory.kishore.factory = org.apache.torque.dsfactory.SharedPool2DataSourceFactory
```

### Key Differences from MySQL

1. **Sequences vs AUTO_INCREMENT:** Oracle uses sequences, MySQL uses AUTO_INCREMENT
2. **VARCHAR2 vs VARCHAR:** Oracle uses VARCHAR2, MySQL uses VARCHAR
3. **NUMBER vs INT:** Oracle uses NUMBER, MySQL uses INT
4. **TIMESTAMP vs DATETIME:** Oracle uses TIMESTAMP, MySQL uses DATETIME
5. **CLOB vs TEXT:** Oracle uses CLOB, MySQL uses TEXT
6. **Schema vs Database:** Oracle uses schemas (users), MySQL uses databases
7. **Case Sensitivity:** Oracle table/column names are case-sensitive when quoted

### Sequence Usage

To get next value from sequence:
```sql
SELECT SEQ_GTP_USER.NEXTVAL FROM DUAL;
```

To get current value:
```sql
SELECT SEQ_GTP_USER.CURRVAL FROM DUAL;
```

---

## Additional Resources

- **Project Documentation:** See `COMPREHENSIVE-TROUBLESHOOTING-GUIDE.md` in project root
- **Application Configuration:** `webapp/src/main/webapp/WEB-INF/conf/Torque.properties`
- **Oracle Documentation:** https://docs.oracle.com/en/database/

---

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the application logs: `apache-tomcat-10.1.44/webapps/uob-t7-portal-mm-tomcat/logs/`
3. Verify database connection in `Torque.properties`
4. Check Oracle alert logs

---

**Last Updated:** January 2026  
**Maintained By:** UOB Development Team

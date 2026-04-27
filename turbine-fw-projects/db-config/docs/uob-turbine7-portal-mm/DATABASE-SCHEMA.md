# Database Schema Documentation

**Project:** UOB Turbine7 Portal MM  
**Database:** kishore  
**Version:** 1.0

---

## Overview

The application uses a custom security schema with 7 tables implementing a role-based access control (RBAC) system with groups, roles, and permissions.

---

## Entity Relationship Diagram

```
┌─────────────┐
│  GTP_USER   │
│─────────────│
│ USER_ID (PK)│
│ LOGIN_NAME  │
│ PASSWORD    │
│ FIRST_NAME  │
│ LAST_NAME   │
│ EMAIL       │
└──────┬──────┘
       │
       │ (1:N)
       │
┌──────▼──────────────────┐
│ GTP_USER_GROUP_ROLE     │
│─────────────────────────│
│ USER_ID (FK)            │
│ GROUP_ID (FK)           │
│ ROLE_ID (FK)            │
│ (PK: USER_ID, GROUP_ID,│
│        ROLE_ID)         │
└──────┬────────┬─────────┘
       │        │
       │ (N:1)  │ (N:1)
       │        │
┌──────▼──┐  ┌──▼─────────┐
│GTP_GROUP│  │ GTP_ROLE   │
│─────────│  │────────────│
│GROUP_ID│  │ ROLE_ID (PK)│
│(PK)    │  │ ROLE_NAME   │
│NAME    │  └──────┬──────┘
└────┬───┘         │
     │             │ (N:M)
     │ (N:M)       │
┌────▼─────────┐  ┌▼──────────────────┐
│GTP_GROUP_ROLE│  │GTP_ROLE_PERMISSION│
│──────────────│  │───────────────────│
│ GROUP_ID (FK)│  │ ROLE_ID (FK)      │
│ ROLE_ID (FK) │  │ PERMISSION_ID (FK)│
└──────────────┘  └──────┬────────────┘
                         │ (N:1)
                    ┌────▼────────────┐
                    │GTP_PERMISSION   │
                    │─────────────────│
                    │PERMISSION_ID(PK)│
                    │PERMISSION_NAME  │
                    └─────────────────┘
```

---

## Table Details

### 1. GTP_USER

**Purpose:** Stores user account information.

**Columns:**

| Column | Type | Null | Key | Default | Description |
|--------|------|------|-----|---------|-------------|
| USER_ID | INT | NO | PK, AI | - | Unique user identifier (auto-increment) |
| LOGIN_NAME | VARCHAR(255) | NO | UK | - | Username for login (unique) |
| PASSWORD_VALUE | VARCHAR(255) | NO | - | - | Password (plain text or hash) |
| FIRST_NAME | VARCHAR(255) | YES | - | NULL | User's first name |
| LAST_NAME | VARCHAR(255) | YES | - | NULL | User's last name |
| EMAIL | VARCHAR(255) | YES | IDX | NULL | User's email address |
| CONFIRM_VALUE | VARCHAR(255) | YES | - | NULL | Account confirmation status |
| CREATED | DATETIME | YES | IDX | CURRENT_TIMESTAMP | Account creation timestamp |
| MODIFIED_DATE | DATETIME | YES | - | CURRENT_TIMESTAMP ON UPDATE | Last modification timestamp |
| LAST_LOGIN | DATETIME | YES | - | NULL | Last login timestamp |
| OBJECTDATA | TEXT | YES | - | NULL | Additional user data (JSON/XML) |

**Indexes:**
- Primary Key: `USER_ID`
- Unique Key: `UK_LOGIN_NAME` on `LOGIN_NAME`
- Index: `IDX_EMAIL` on `EMAIL`
- Index: `IDX_CREATED` on `CREATED`

**Constraints:**
- `LOGIN_NAME` must be unique

**Notes:**
- The `anon` user must exist for anonymous/unauthenticated access
- Password storage depends on Turbine's crypto service configuration
- For ClearCrypt (default), passwords are stored as plain text
- For production, use proper password hashing

---

### 2. GTP_ROLE

**Purpose:** Stores security roles.

**Columns:**

| Column | Type | Null | Key | Default | Description |
|--------|------|------|-----|---------|-------------|
| ROLE_ID | INT | NO | PK, AI | - | Unique role identifier (auto-increment) |
| ROLE_NAME | VARCHAR(255) | NO | UK | - | Role name (unique) |

**Indexes:**
- Primary Key: `ROLE_ID`
- Unique Key: `UK_ROLE_NAME` on `ROLE_NAME`

**Standard Roles:**
- `ADMIN` - Administrator with full access
- `MANAGER` - Manager with user management access
- `USER` - Regular user with basic access
- `ANONYMOUS` - Anonymous/unauthenticated user

---

### 3. GTP_PERMISSION

**Purpose:** Stores security permissions.

**Columns:**

| Column | Type | Null | Key | Default | Description |
|--------|------|------|-----|---------|-------------|
| PERMISSION_ID | INT | NO | PK, AI | - | Unique permission identifier (auto-increment) |
| PERMISSION_NAME | VARCHAR(255) | NO | UK | - | Permission name (unique) |

**Indexes:**
- Primary Key: `PERMISSION_ID`
- Unique Key: `UK_PERMISSION_NAME` on `PERMISSION_NAME`

**Standard Permissions:**
- `VIEW_DASHBOARD` - View dashboard/home page
- `MANAGE_USERS` - Create, update, delete users
- `ADMIN_ACCESS` - Full administrative access

---

### 4. GTP_GROUP

**Purpose:** Stores user groups.

**Columns:**

| Column | Type | Null | Key | Default | Description |
|--------|------|------|-----|---------|-------------|
| GROUP_ID | INT | NO | PK, AI | - | Unique group identifier (auto-increment) |
| GROUP_NAME | VARCHAR(255) | NO | UK | - | Group name (unique) |

**Indexes:**
- Primary Key: `GROUP_ID`
- Unique Key: `UK_GROUP_NAME` on `GROUP_NAME`

**Standard Groups:**
- `ADMINISTRATORS` - Administrator group
- `REGULAR_USERS` - Regular user group
- `GUESTS` - Guest/anonymous user group

---

### 5. GTP_ROLE_PERMISSION

**Purpose:** Maps roles to permissions (Many-to-Many relationship).

**Columns:**

| Column | Type | Null | Key | Default | Description |
|--------|------|------|-----|---------|-------------|
| ROLE_ID | INT | NO | PK, FK | - | Reference to GTP_ROLE.ROLE_ID |
| PERMISSION_ID | INT | NO | PK, FK | - | Reference to GTP_PERMISSION.PERMISSION_ID |

**Indexes:**
- Primary Key: `(ROLE_ID, PERMISSION_ID)`
- Foreign Key: `FK_ROLE_PERMISSION_ROLE` → `GTP_ROLE(ROLE_ID)` ON DELETE CASCADE
- Foreign Key: `FK_ROLE_PERMISSION_PERMISSION` → `GTP_PERMISSION(PERMISSION_ID)` ON DELETE CASCADE
- Index: `IDX_ROLE_ID` on `ROLE_ID`
- Index: `IDX_PERMISSION_ID` on `PERMISSION_ID`

**Constraints:**
- Each role-permission combination is unique
- Deleting a role or permission cascades to remove mappings

**Standard Mappings:**
- `ADMIN` → All permissions (VIEW_DASHBOARD, MANAGE_USERS, ADMIN_ACCESS)
- `MANAGER` → VIEW_DASHBOARD, MANAGE_USERS
- `USER` → VIEW_DASHBOARD
- `ANONYMOUS` → VIEW_DASHBOARD

---

### 6. GTP_GROUP_ROLE

**Purpose:** Maps groups to roles (Many-to-Many relationship).

**Columns:**

| Column | Type | Null | Key | Default | Description |
|--------|------|------|-----|---------|-------------|
| GROUP_ID | INT | NO | PK, FK | - | Reference to GTP_GROUP.GROUP_ID |
| ROLE_ID | INT | NO | PK, FK | - | Reference to GTP_ROLE.ROLE_ID |

**Indexes:**
- Primary Key: `(GROUP_ID, ROLE_ID)`
- Foreign Key: `FK_GROUP_ROLE_GROUP` → `GTP_GROUP(GROUP_ID)` ON DELETE CASCADE
- Foreign Key: `FK_GROUP_ROLE_ROLE` → `GTP_ROLE(ROLE_ID)` ON DELETE CASCADE
- Index: `IDX_GROUP_ID` on `GROUP_ID`
- Index: `IDX_ROLE_ID` on `ROLE_ID`

**Constraints:**
- Each group-role combination is unique
- Deleting a group or role cascades to remove mappings

**Standard Mappings:**
- `ADMINISTRATORS` → ADMIN
- `REGULAR_USERS` → MANAGER, USER
- `GUESTS` → ANONYMOUS

---

### 7. GTP_USER_GROUP_ROLE

**Purpose:** Maps users to groups and roles (Many-to-Many relationship).

**Columns:**

| Column | Type | Null | Key | Default | Description |
|--------|------|------|-----|---------|-------------|
| USER_ID | INT | NO | PK, FK | - | Reference to GTP_USER.USER_ID |
| GROUP_ID | INT | NO | PK, FK | - | Reference to GTP_GROUP.GROUP_ID |
| ROLE_ID | INT | NO | PK, FK | - | Reference to GTP_ROLE.ROLE_ID |

**Indexes:**
- Primary Key: `(USER_ID, GROUP_ID, ROLE_ID)`
- Foreign Key: `FK_USER_GROUP_ROLE_USER` → `GTP_USER(USER_ID)` ON DELETE CASCADE
- Foreign Key: `FK_USER_GROUP_ROLE_GROUP` → `GTP_GROUP(GROUP_ID)` ON DELETE CASCADE
- Foreign Key: `FK_USER_GROUP_ROLE_ROLE` → `GTP_ROLE(ROLE_ID)` ON DELETE CASCADE
- Index: `IDX_USER_ID` on `USER_ID`
- Index: `IDX_GROUP_ID` on `GROUP_ID`
- Index: `IDX_ROLE_ID` on `ROLE_ID`
- Index: `IDX_USER_GROUP` on `(USER_ID, GROUP_ID)`

**Constraints:**
- Each user-group-role combination is unique
- Deleting a user, group, or role cascades to remove mappings

**Notes:**
- A user can have multiple group-role assignments
- This table is the primary source for user access control
- The application queries this table directly for user permissions

---

## Access Control Model

### Hierarchy

```
User
  └─> Group (via GTP_USER_GROUP_ROLE)
      └─> Role (via GTP_USER_GROUP_ROLE)
          └─> Permission (via GTP_ROLE_PERMISSION)
```

### Example

**User:** `admin`
- **Group:** `ADMINISTRATORS`
- **Role:** `ADMIN`
- **Permissions:** `VIEW_DASHBOARD`, `MANAGE_USERS`, `ADMIN_ACCESS`

**Query Path:**
1. Find user in `GTP_USER` (LOGIN_NAME = 'admin')
2. Find group-role assignments in `GTP_USER_GROUP_ROLE` (USER_ID = 1)
3. Find permissions in `GTP_ROLE_PERMISSION` (ROLE_ID = 1)

---

## Data Integrity Rules

1. **Cascade Deletes:**
   - Deleting a user removes all `GTP_USER_GROUP_ROLE` entries
   - Deleting a role removes all `GTP_ROLE_PERMISSION` and `GTP_GROUP_ROLE` entries
   - Deleting a group removes all `GTP_GROUP_ROLE` entries
   - Deleting a permission removes all `GTP_ROLE_PERMISSION` entries

2. **Required Data:**
   - Anonymous user (`anon`) must exist
   - Anonymous role (`ANONYMOUS`) must exist
   - Anonymous user must be assigned to GUESTS group with ANONYMOUS role

3. **Unique Constraints:**
   - `LOGIN_NAME` must be unique
   - `ROLE_NAME` must be unique
   - `PERMISSION_NAME` must be unique
   - `GROUP_NAME` must be unique
   - User-Group-Role combinations must be unique

---

## Common Queries

### Get User Permissions

```sql
SELECT DISTINCT p.PERMISSION_NAME
FROM GTP_USER u
JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
WHERE u.LOGIN_NAME = 'admin';
```

### Get User Groups and Roles

```sql
SELECT 
    g.GROUP_NAME,
    r.ROLE_NAME
FROM GTP_USER u
JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
WHERE u.LOGIN_NAME = 'admin';
```

### Check if User Has Permission

```sql
SELECT COUNT(*) > 0 AS has_permission
FROM GTP_USER u
JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
WHERE u.LOGIN_NAME = 'admin'
  AND p.PERMISSION_NAME = 'ADMIN_ACCESS';
```

---

## Maintenance

### Backup

```bash
mysqldump -u kishore -pKish1381@ kishore > backup.sql
```

### Restore

```bash
mysql -u kishore -pKish1381@ kishore < backup.sql
```

### Reset All Data

```sql
DELETE FROM GTP_USER_GROUP_ROLE;
DELETE FROM GTP_GROUP_ROLE;
DELETE FROM GTP_ROLE_PERMISSION;
DELETE FROM GTP_USER;
DELETE FROM GTP_GROUP;
DELETE FROM GTP_ROLE;
DELETE FROM GTP_PERMISSION;
-- Then run 03-load-test-data.sql
```

---

**Last Updated:** January 2026

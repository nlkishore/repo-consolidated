# User Insertion Validation Guide

## Overview
This guide helps you validate whether new users created through the web interface are being properly inserted into the database.

## Validation Steps

### Step 1: Check Current User Count
Before creating a new user, note the current count:

```sql
USE kishore;
SELECT COUNT(*) as total_users FROM GTP_USER;
```

### Step 2: Create a New User
1. Log in as admin
2. Navigate to: **Administration → Add New User**
3. Fill in the form:
   - Username: `testuser1`
   - First Name: `Test`
   - Last Name: `User`
   - Email: `testuser1@example.com`
   - Password: `password123`
4. Click **Save** or **Submit**

### Step 3: Check Application Logs
After creating the user, check `application.log` for:

```
FluxUserAction.doInsert() - Creating new user: testuser1
FluxUserAction.doInsert() - User instance created, calling security.addUser()
FluxUserAction.doInsert() - User added successfully, user ID: <id>
FluxUserAction.doInsert() - Password set for user: testuser1
```

If you see errors, check the exception details.

### Step 4: Check SQL Logs
Check `sql.log` for INSERT statements:

```
INSERT INTO GTP_USER (LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, ...)
```

### Step 5: Verify in Database
Run these SQL queries:

```sql
USE kishore;

-- Check if the new user exists
SELECT 
    USER_ID, 
    LOGIN_NAME, 
    FIRST_NAME, 
    LAST_NAME, 
    EMAIL, 
    CREATED,
    MODIFIED_DATE
FROM GTP_USER 
WHERE LOGIN_NAME = 'testuser1';

-- Check all recent users (last hour)
SELECT 
    USER_ID, 
    LOGIN_NAME, 
    FIRST_NAME, 
    LAST_NAME, 
    EMAIL, 
    CREATED
FROM GTP_USER 
WHERE CREATED >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY CREATED DESC;

-- Check total user count (should have increased)
SELECT COUNT(*) as total_users FROM GTP_USER;
```

### Step 6: Verify User Can Login
1. Logout
2. Try to login with the new user credentials
3. If login succeeds, the user was created correctly

## Troubleshooting

### User Not Appearing in Database

1. **Check for Errors in Logs**
   - Look for exceptions in `application.log`
   - Check for SQL errors in `sql.log`

2. **Check User Already Exists**
   - The system prevents duplicate usernames
   - Check if user already exists:
     ```sql
     SELECT * FROM GTP_USER WHERE LOGIN_NAME = 'testuser1';
     ```

3. **Check Transaction Rollback**
   - If there's an error after INSERT, the transaction might be rolled back
   - Check for constraint violations (foreign keys, unique constraints)

4. **Check Database Connection**
   - Verify database connection is working
   - Check if other operations (like login) work

### Common Issues

1. **Username Already Exists**
   - Error message: "The user already exists"
   - Solution: Use a different username

2. **Password Validation Failed**
   - Check password requirements
   - Ensure password is not empty

3. **Database Constraint Violation**
   - Check for NOT NULL constraints
   - Verify foreign key relationships

## Expected Database State

After creating a user, you should see:

1. **GTP_USER table**: New row with user details
2. **GTP_USER_GROUP_ROLE table**: May or may not have entries (depends on default group/role assignment)

## SQL Validation Script

A complete validation script is available at:
`C:\Turbineprojects\uob-turbine7-portal-mm\validate-user-insertion.sql`

Run this script in MySQL Workbench to check:
- All users
- Recent insertions
- User count
- Users without group/role assignments

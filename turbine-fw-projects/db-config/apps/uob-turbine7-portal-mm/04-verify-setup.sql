-- ========================================================================
-- UOB Turbine7 Portal MM - Setup Verification Script
-- ========================================================================
-- This script verifies the database setup is correct
-- Run this as kishore user:
--   mysql -u kishore -pKish1381@ kishore < 04-verify-setup.sql
-- ========================================================================

USE kishore;

-- ========================================================================
-- 1. Verify Database Exists
-- ========================================================================
SELECT '========================================' AS '';
SELECT '1. Database Verification' AS '';
SELECT '========================================' AS '';
SELECT DATABASE() AS 'Current Database';

-- ========================================================================
-- 2. Verify Tables Exist
-- ========================================================================
SELECT '========================================' AS '';
SELECT '2. Table Verification' AS '';
SELECT '========================================' AS '';
SHOW TABLES;

-- ========================================================================
-- 3. Count Records in Each Table
-- ========================================================================
SELECT '========================================' AS '';
SELECT '3. Record Counts' AS '';
SELECT '========================================' AS '';
SELECT 
    'GTP_USER' AS TableName,
    COUNT(*) AS RecordCount
FROM GTP_USER
UNION ALL
SELECT 
    'GTP_ROLE',
    COUNT(*)
FROM GTP_ROLE
UNION ALL
SELECT 
    'GTP_PERMISSION',
    COUNT(*)
FROM GTP_PERMISSION
UNION ALL
SELECT 
    'GTP_GROUP',
    COUNT(*)
FROM GTP_GROUP
UNION ALL
SELECT 
    'GTP_ROLE_PERMISSION',
    COUNT(*)
FROM GTP_ROLE_PERMISSION
UNION ALL
SELECT 
    'GTP_GROUP_ROLE',
    COUNT(*)
FROM GTP_GROUP_ROLE
UNION ALL
SELECT 
    'GTP_USER_GROUP_ROLE',
    COUNT(*)
FROM GTP_USER_GROUP_ROLE;

-- ========================================================================
-- 4. Display Sample Data - Users
-- ========================================================================
SELECT '========================================' AS '';
SELECT '4. Sample Users' AS '';
SELECT '========================================' AS '';
SELECT 
    USER_ID,
    LOGIN_NAME,
    FIRST_NAME,
    LAST_NAME,
    EMAIL,
    CREATED
FROM GTP_USER
ORDER BY USER_ID;

-- ========================================================================
-- 5. Display Sample Data - Roles
-- ========================================================================
SELECT '========================================' AS '';
SELECT '5. Sample Roles' AS '';
SELECT '========================================' AS '';
SELECT 
    ROLE_ID,
    ROLE_NAME
FROM GTP_ROLE
ORDER BY ROLE_ID;

-- ========================================================================
-- 6. Display Sample Data - Permissions
-- ========================================================================
SELECT '========================================' AS '';
SELECT '6. Sample Permissions' AS '';
SELECT '========================================' AS '';
SELECT 
    PERMISSION_ID,
    PERMISSION_NAME
FROM GTP_PERMISSION
ORDER BY PERMISSION_ID;

-- ========================================================================
-- 7. Display Sample Data - Groups
-- ========================================================================
SELECT '========================================' AS '';
SELECT '7. Sample Groups' AS '';
SELECT '========================================' AS '';
SELECT 
    GROUP_ID,
    GROUP_NAME
FROM GTP_GROUP
ORDER BY GROUP_ID;

-- ========================================================================
-- 8. Verify Role-Permission Mappings
-- ========================================================================
SELECT '========================================' AS '';
SELECT '8. Role-Permission Mappings' AS '';
SELECT '========================================' AS '';
SELECT 
    r.ROLE_NAME,
    p.PERMISSION_NAME
FROM GTP_ROLE r
JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
ORDER BY r.ROLE_NAME, p.PERMISSION_NAME;

-- ========================================================================
-- 9. Verify Group-Role Mappings
-- ========================================================================
SELECT '========================================' AS '';
SELECT '9. Group-Role Mappings' AS '';
SELECT '========================================' AS '';
SELECT 
    g.GROUP_NAME,
    r.ROLE_NAME
FROM GTP_GROUP g
JOIN GTP_GROUP_ROLE gr ON g.GROUP_ID = gr.GROUP_ID
JOIN GTP_ROLE r ON gr.ROLE_ID = r.ROLE_ID
ORDER BY g.GROUP_NAME, r.ROLE_NAME;

-- ========================================================================
-- 10. Verify Complete User Access Matrix
-- ========================================================================
SELECT '========================================' AS '';
SELECT '10. Complete User Access Matrix' AS '';
SELECT '========================================' AS '';
SELECT 
    u.LOGIN_NAME AS 'Username',
    u.FIRST_NAME AS 'First Name',
    u.LAST_NAME AS 'Last Name',
    g.GROUP_NAME AS 'Group',
    r.ROLE_NAME AS 'Role',
    p.PERMISSION_NAME AS 'Permission'
FROM GTP_USER u
JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME, p.PERMISSION_NAME;

-- ========================================================================
-- 11. Verify Anonymous User Exists
-- ========================================================================
SELECT '========================================' AS '';
SELECT '11. Anonymous User Verification' AS '';
SELECT '========================================' AS '';
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ Anonymous user exists'
        ELSE '✗ WARNING: Anonymous user not found!'
    END AS Status
FROM GTP_USER
WHERE LOGIN_NAME = 'anon';

-- ========================================================================
-- 12. Verify Foreign Key Constraints
-- ========================================================================
SELECT '========================================' AS '';
SELECT '12. Foreign Key Constraints Verification' AS '';
SELECT '========================================' AS '';
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'kishore'
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

-- ========================================================================
-- 13. Summary
-- ========================================================================
SELECT '========================================' AS '';
SELECT '13. Setup Verification Summary' AS '';
SELECT '========================================' AS '';
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM GTP_USER) >= 6 THEN '✓ Users: OK'
        ELSE '✗ Users: Missing or incomplete'
    END AS 'Users',
    CASE 
        WHEN (SELECT COUNT(*) FROM GTP_ROLE) >= 4 THEN '✓ Roles: OK'
        ELSE '✗ Roles: Missing or incomplete'
    END AS 'Roles',
    CASE 
        WHEN (SELECT COUNT(*) FROM GTP_PERMISSION) >= 3 THEN '✓ Permissions: OK'
        ELSE '✗ Permissions: Missing or incomplete'
    END AS 'Permissions',
    CASE 
        WHEN (SELECT COUNT(*) FROM GTP_GROUP) >= 3 THEN '✓ Groups: OK'
        ELSE '✗ Groups: Missing or incomplete'
    END AS 'Groups',
    CASE 
        WHEN (SELECT COUNT(*) FROM GTP_USER WHERE LOGIN_NAME = 'anon') > 0 THEN '✓ Anonymous User: OK'
        ELSE '✗ Anonymous User: Missing'
    END AS 'Anonymous User';

SELECT '========================================' AS '';
SELECT 'Verification Complete!' AS '';
SELECT '========================================' AS '';

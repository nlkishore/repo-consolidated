-- ========================================================================
-- Assign Roles, Groups, and Permissions to ALL Existing Users
-- ========================================================================
-- This script ensures ALL users in GTP_USER table have:gtp_user_group_rolegtp_user_group_role
--   - At least one group assignment
--   - At least one role assignment
--   - Permissions through their roles
-- ========================================================================
-- Usage: Run this after gtp-test-data.sql or to update existing users
-- ========================================================================

USE kishore;

SET FOREIGN_KEY_CHECKS = 0;

-- ========================================================================
-- Step 1: Ensure required Groups, Roles, and Permissions exist
-- ========================================================================

-- Insert Groups if they don't exist
INSERT IGNORE INTO GTP_GROUP (GROUP_ID, GROUP_NAME) VALUES
(1, 'ADMINISTRATORS'),
(2, 'REGULAR_USERS'),
(3, 'GUESTS');

-- Insert Roles if they don't exist
INSERT IGNORE INTO GTP_ROLE (ROLE_ID, ROLE_NAME) VALUES
(1, 'ADMIN'),
(2, 'MANAGER'),
(3, 'USER'),
(4, 'ANONYMOUS');

-- Insert Permissions if they don't exist
INSERT IGNORE INTO GTP_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES
(1, 'VIEW_DASHBOARD'),
(2, 'MANAGE_USERS'),
(3, 'ADMIN_ACCESS');

-- ========================================================================
-- Step 2: Ensure Role-Permission mappings exist
-- ========================================================================

-- ADMIN role has all permissions
INSERT IGNORE INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(1, 1), -- ADMIN -> VIEW_DASHBOARD
(1, 2), -- ADMIN -> MANAGE_USERS
(1, 3); -- ADMIN -> ADMIN_ACCESS

-- MANAGER role has VIEW_DASHBOARD and MANAGE_USERS
INSERT IGNORE INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(2, 1), -- MANAGER -> VIEW_DASHBOARD
(2, 2); -- MANAGER -> MANAGE_USERS

-- USER role has only VIEW_DASHBOARD
INSERT IGNORE INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(3, 1); -- USER -> VIEW_DASHBOARD

-- ANONYMOUS role has only VIEW_DASHBOARD
INSERT IGNORE INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(4, 1); -- ANONYMOUS -> VIEW_DASHBOARD

-- ========================================================================
-- Step 3: Ensure Group-Role mappings exist
-- ========================================================================

-- ADMINISTRATORS group has ADMIN role
INSERT IGNORE INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID) VALUES
(1, 1); -- ADMINISTRATORS -> ADMIN

-- REGULAR_USERS group has MANAGER and USER roles
INSERT IGNORE INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID) VALUES
(2, 2), -- REGULAR_USERS -> MANAGER
(2, 3); -- REGULAR_USERS -> USER

-- GUESTS group has ANONYMOUS role
INSERT IGNORE INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID) VALUES
(3, 4); -- GUESTS -> ANONYMOUS

-- ========================================================================
-- Step 4: Assign roles to ALL existing users
-- ========================================================================
-- Strategy:
--   - Users with 'admin' in login_name -> ADMINISTRATORS group, ADMIN role
--   - Users with 'manager' in login_name -> REGULAR_USERS group, MANAGER role
--   - Users with 'anon' in login_name -> GUESTS group, ANONYMOUS role
--   - All other users -> REGULAR_USERS group, USER role
-- ========================================================================

-- Delete existing user-group-role mappings to avoid duplicates
-- (Comment out if you want to preserve existing mappings)
-- DELETE FROM GTP_USER_GROUP_ROLE;

-- Assign ADMIN role to admin users
INSERT IGNORE INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
SELECT u.USER_ID, 1, 1  -- ADMINISTRATORS group, ADMIN role
FROM GTP_USER u
WHERE LOWER(u.LOGIN_NAME) LIKE '%admin%'
  AND u.LOGIN_NAME != 'anon'
  AND NOT EXISTS (
    SELECT 1 FROM GTP_USER_GROUP_ROLE ugr 
    WHERE ugr.USER_ID = u.USER_ID AND ugr.GROUP_ID = 1 AND ugr.ROLE_ID = 1
  );

-- Assign MANAGER role to manager users
INSERT IGNORE INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
SELECT u.USER_ID, 2, 2  -- REGULAR_USERS group, MANAGER role
FROM GTP_USER u
WHERE LOWER(u.LOGIN_NAME) LIKE '%manager%'
  AND u.LOGIN_NAME != 'anon'
  AND NOT EXISTS (
    SELECT 1 FROM GTP_USER_GROUP_ROLE ugr 
    WHERE ugr.USER_ID = u.USER_ID AND ugr.GROUP_ID = 2 AND ugr.ROLE_ID = 2
  );

-- Assign ANONYMOUS role to anonymous user
INSERT IGNORE INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
SELECT u.USER_ID, 3, 4  -- GUESTS group, ANONYMOUS role
FROM GTP_USER u
WHERE LOWER(u.LOGIN_NAME) = 'anon'
  AND NOT EXISTS (
    SELECT 1 FROM GTP_USER_GROUP_ROLE ugr 
    WHERE ugr.USER_ID = u.USER_ID AND ugr.GROUP_ID = 3 AND ugr.ROLE_ID = 4
  );

-- Assign USER role to all other users (default assignment)
INSERT IGNORE INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
SELECT u.USER_ID, 2, 3  -- REGULAR_USERS group, USER role
FROM GTP_USER u
WHERE u.LOGIN_NAME != 'anon'
  AND LOWER(u.LOGIN_NAME) NOT LIKE '%admin%'
  AND LOWER(u.LOGIN_NAME) NOT LIKE '%manager%'
  AND NOT EXISTS (
    SELECT 1 FROM GTP_USER_GROUP_ROLE ugr 
    WHERE ugr.USER_ID = u.USER_ID
  );

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================================
-- Verification Queries
-- ========================================================================

-- View all users with their group and role assignments
SELECT 
    u.USER_ID,
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
GROUP BY u.USER_ID, u.LOGIN_NAME, u.FIRST_NAME, u.LAST_NAME, g.GROUP_NAME, r.ROLE_NAME
ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME;

-- Count users without any group/role assignment
SELECT 
    COUNT(*) as USERS_WITHOUT_ASSIGNMENTS
FROM GTP_USER u
WHERE NOT EXISTS (
    SELECT 1 FROM GTP_USER_GROUP_ROLE ugr WHERE ugr.USER_ID = u.USER_ID
);

-- View complete user access (users, groups, roles, permissions)
SELECT 
    u.LOGIN_NAME,
    u.FIRST_NAME,
    u.LAST_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME,
    GROUP_CONCAT(p.PERMISSION_NAME ORDER BY p.PERMISSION_NAME SEPARATOR ', ') as PERMISSIONS
FROM GTP_USER u
JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
GROUP BY u.LOGIN_NAME, u.FIRST_NAME, u.LAST_NAME, g.GROUP_NAME, r.ROLE_NAME
ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME;

-- ========================================================================
-- End of Script
-- ========================================================================

-- ========================================================================
-- GTP Security Test Data
-- ========================================================================
-- This script inserts test data for the UOB Turbine 7 Portal
-- 5 Users, 3 Roles, 3 Permissions, 2 Groups
-- ========================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data (optional - comment out if you want to keep existing data)
-- Note: Table names are case-sensitive in MySQL on Linux, but case-insensitive on Windows
DELETE FROM gtp_user_group_role;
DELETE FROM gtp_group_role;
DELETE FROM gtp_role_permission;
DELETE FROM gtp_user;
DELETE FROM gtp_group;
DELETE FROM gtp_role;
DELETE FROM gtp_permission;

-- ========================================================================
-- Insert Permissions (3 permissions)
-- ========================================================================
INSERT INTO gtp_permission (PERMISSION_ID, PERMISSION_NAME) VALUES
(1, 'VIEW_DASHBOARD'),
(2, 'MANAGE_USERS'),
(3, 'ADMIN_ACCESS');

-- ========================================================================
-- Insert Roles (3 roles)
-- ========================================================================
INSERT INTO gtp_role (ROLE_ID, ROLE_NAME) VALUES
(1, 'ADMIN'),
(2, 'MANAGER'),
(3, 'USER');

-- ========================================================================
-- Insert Groups (2 groups)
-- ========================================================================
INSERT INTO gtp_group (GROUP_ID, GROUP_NAME) VALUES
(1, 'ADMINISTRATORS'),
(2, 'REGULAR_USERS');

-- ========================================================================
-- Map Roles to Permissions (Role-Permission mapping)
-- ========================================================================
-- ADMIN role has all permissions
INSERT INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(1, 1), -- ADMIN -> VIEW_DASHBOARD
(1, 2), -- ADMIN -> MANAGE_USERS
(1, 3); -- ADMIN -> ADMIN_ACCESS

-- MANAGER role has VIEW_DASHBOARD and MANAGE_USERS
INSERT INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(2, 1), -- MANAGER -> VIEW_DASHBOARD
(2, 2); -- MANAGER -> MANAGE_USERS

-- USER role has only VIEW_DASHBOARD
INSERT INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(3, 1); -- USER -> VIEW_DASHBOARD

-- ========================================================================
-- Map Groups to Roles (Group-Role mapping)
-- ========================================================================
-- ADMINISTRATORS group has ADMIN role
INSERT INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID) VALUES
(1, 1); -- ADMINISTRATORS -> ADMIN

-- REGULAR_USERS group has MANAGER and USER roles
INSERT INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID) VALUES
(2, 2), -- REGULAR_USERS -> MANAGER
(2, 3); -- REGULAR_USERS -> USER

-- ========================================================================
-- Insert Users (5 users)
-- ========================================================================
-- Note: Password values are MD5 hashed. For testing, we'll use simple passwords
-- In production, use proper password hashing
-- Default password for all test users: "password123" (MD5: 482c811da5d5b4bc6d497ffa98491e38)
-- For simplicity, we'll use a simple hash. In real app, use Turbine's password hashing

INSERT INTO gtp_user (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE, CREATED, MODIFIED_DATE) VALUES
(1, 'admin', 'password123', 'Admin', 'User', 'admin@uob.com', 'Y', NOW(), NOW()),
(2, 'manager1', 'password123', 'John', 'Manager', 'manager1@uob.com', 'Y', NOW(), NOW()),
(3, 'manager2', 'password123', 'Jane', 'Manager', 'manager2@uob.com', 'Y', NOW(), NOW()),
(4, 'user1', 'password123', 'Bob', 'User', 'user1@uob.com', 'Y', NOW(), NOW()),
(5, 'user2', 'password123', 'Alice', 'User', 'user2@uob.com', 'Y', NOW(), NOW());

-- ========================================================================
-- Map Users to Groups and Roles (User-Group-Role mapping)
-- ========================================================================
-- admin user -> ADMINISTRATORS group -> ADMIN role
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID) VALUES
(1, 1, 1); -- admin -> ADMINISTRATORS -> ADMIN

-- manager1 user -> REGULAR_USERS group -> MANAGER role
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID) VALUES
(2, 2, 2); -- manager1 -> REGULAR_USERS -> MANAGER

-- manager2 user -> REGULAR_USERS group -> MANAGER role
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID) VALUES
(3, 2, 2); -- manager2 -> REGULAR_USERS -> MANAGER

-- user1 user -> REGULAR_USERS group -> USER role
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID) VALUES
(4, 2, 3); -- user1 -> REGULAR_USERS -> USER

-- user2 user -> REGULAR_USERS group -> USER role
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID) VALUES
(5, 2, 3); -- user2 -> REGULAR_USERS -> USER

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================================
-- Verification Queries
-- ========================================================================
-- Uncomment to verify data:
-- SELECT * FROM GTP_USER;
-- SELECT * FROM GTP_ROLE;
-- SELECT * FROM GTP_PERMISSION;
-- SELECT * FROM GTP_GROUP;
-- SELECT u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME, p.PERMISSION_NAME
-- FROM GTP_USER u
-- JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
-- JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
-- JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
-- JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
-- JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
-- ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME, p.PERMISSION_NAME;

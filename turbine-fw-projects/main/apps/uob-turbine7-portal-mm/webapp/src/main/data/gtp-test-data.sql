-- ========================================================================
-- GTP Security Test Data for UOB Turbine 7 Portal Multi-Module
-- ========================================================================
-- Database: kishore
-- Tables: GTP_USER, GTP_ROLE, GTP_PERMISSION, GTP_GROUP, 
--         GTP_ROLE_PERMISSION, GTP_GROUP_ROLE, GTP_USER_GROUP_ROLE
-- ========================================================================
-- This script inserts test data:
--   - 6 Users (5 regular + 1 anonymous)
--   - 4 Roles (ADMIN, MANAGER, USER, ANONYMOUS)
--   - 3 Permissions (VIEW_DASHBOARD, MANAGE_USERS, ADMIN_ACCESS)
--   - 3 Groups (ADMINISTRATORS, REGULAR_USERS, GUESTS)
-- ========================================================================
-- Default Password for all users: "password123"
-- Password Hash: MD5 hash of "password123" = "482c811da5d5b4bc6d497ffa98491e38"
-- Note: Turbine uses crypto service. If using ClearCrypt, plain text works.
--       For production, use proper password hashing.
-- ========================================================================

USE kishore;

SET FOREIGN_KEY_CHECKS = 0;

-- ========================================================================
-- Clear existing data (optional - comment out if you want to keep existing data)
-- ========================================================================
DELETE FROM GTP_USER_GROUP_ROLE;
DELETE FROM GTP_GROUP_ROLE;
DELETE FROM GTP_ROLE_PERMISSION;
DELETE FROM GTP_USER;
DELETE FROM GTP_GROUP;
DELETE FROM GTP_ROLE;
DELETE FROM GTP_PERMISSION;

-- Reset AUTO_INCREMENT
ALTER TABLE GTP_USER AUTO_INCREMENT = 1;
ALTER TABLE GTP_ROLE AUTO_INCREMENT = 1;
ALTER TABLE GTP_PERMISSION AUTO_INCREMENT = 1;
ALTER TABLE GTP_GROUP AUTO_INCREMENT = 1;

-- ========================================================================
-- Insert Permissions (3 permissions)
-- ========================================================================
INSERT INTO GTP_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES
(1, 'VIEW_DASHBOARD'),
(2, 'MANAGE_USERS'),
(3, 'ADMIN_ACCESS');

-- ========================================================================
-- Insert Roles (4 roles - includes anonymous role)
-- ========================================================================
INSERT INTO GTP_ROLE (ROLE_ID, ROLE_NAME) VALUES
(1, 'ADMIN'),
(2, 'MANAGER'),
(3, 'USER'),
(4, 'ANONYMOUS');

-- ========================================================================
-- Insert Groups (3 groups - includes guests group)
-- ========================================================================
INSERT INTO GTP_GROUP (GROUP_ID, GROUP_NAME) VALUES
(1, 'ADMINISTRATORS'),
(2, 'REGULAR_USERS'),
(3, 'GUESTS');

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

-- ANONYMOUS role has only VIEW_DASHBOARD (minimal read-only access)
INSERT INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID) VALUES
(4, 1); -- ANONYMOUS -> VIEW_DASHBOARD

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

-- GUESTS group has ANONYMOUS role
INSERT INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID) VALUES
(3, 4); -- GUESTS -> ANONYMOUS

-- ========================================================================
-- Insert Users (6 users - includes anonymous user)
-- ========================================================================
-- Password: "password123" for regular users
-- Anonymous user password: "anon" (Turbine default, typically not used for login)
-- For ClearCrypt (default in many Turbine setups), use plain text
-- For JavaCrypt/MD5, use: '482c811da5d5b4bc6d497ffa98491e38'
-- For production, use proper password hashing via Turbine's crypto service

INSERT INTO GTP_USER (
    USER_ID, 
    LOGIN_NAME, 
    PASSWORD_VALUE, 
    FIRST_NAME, 
    LAST_NAME, 
    EMAIL, 
    CONFIRM_VALUE, 
    CREATED, 
    MODIFIED_DATE
) VALUES
(1, 'admin', 'password123', 'Admin', 'User', 'admin@uob.com', 'Y', NOW(), NOW()),
(2, 'manager1', 'password123', 'John', 'Manager', 'manager1@uob.com', 'Y', NOW(), NOW()),
(3, 'manager2', 'password123', 'Jane', 'Manager', 'manager2@uob.com', 'Y', NOW(), NOW()),
(4, 'user1', 'password123', 'Bob', 'User', 'user1@uob.com', 'Y', NOW(), NOW()),
(5, 'user2', 'password123', 'Alice', 'User', 'user2@uob.com', 'Y', NOW(), NOW()),
(6, 'anon', 'anon', 'Anonymous', 'User', 'anon@uob.com', 'Y', NOW(), NOW());

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

-- anon (anonymous) user -> GUESTS group -> ANONYMOUS role
INSERT INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID) VALUES
(6, 3, 4); -- anon -> GUESTS -> ANONYMOUS

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================================
-- Verification Queries
-- ========================================================================
-- Uncomment the following queries to verify the data:

-- View all users
-- SELECT * FROM GTP_USER;

-- View all roles
-- SELECT * FROM GTP_ROLE;

-- View all permissions
-- SELECT * FROM GTP_PERMISSION;

-- View all groups
-- SELECT * FROM GTP_GROUP;

-- View role-permission mappings
-- SELECT r.ROLE_NAME, p.PERMISSION_NAME 
-- FROM GTP_ROLE r
-- JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
-- JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
-- ORDER BY r.ROLE_NAME, p.PERMISSION_NAME;

-- View group-role mappings
-- SELECT g.GROUP_NAME, r.ROLE_NAME
-- FROM GTP_GROUP g
-- JOIN GTP_GROUP_ROLE gr ON g.GROUP_ID = gr.GROUP_ID
-- JOIN GTP_ROLE r ON gr.ROLE_ID = r.ROLE_ID
-- ORDER BY g.GROUP_NAME, r.ROLE_NAME;

-- View complete user access (users, groups, roles, permissions)
-- SELECT 
--     u.LOGIN_NAME,
--     u.FIRST_NAME,
--     u.LAST_NAME,
--     g.GROUP_NAME,
--     r.ROLE_NAME,
--     p.PERMISSION_NAME
-- FROM GTP_USER u
-- JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
-- JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
-- JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
-- JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
-- JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
-- ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME, p.PERMISSION_NAME;

-- ========================================================================
-- Test User Credentials
-- ========================================================================
-- Username: admin       Password: password123  (ADMIN role, all permissions)
-- Username: manager1   Password: password123  (MANAGER role, VIEW_DASHBOARD, MANAGE_USERS)
-- Username: manager2   Password: password123  (MANAGER role, VIEW_DASHBOARD, MANAGE_USERS)
-- Username: user1      Password: password123  (USER role, VIEW_DASHBOARD only)
-- Username: user2      Password: password123  (USER role, VIEW_DASHBOARD only)
-- Username: anon       Password: anon        (ANONYMOUS role, VIEW_DASHBOARD only - for unauthenticated access)
-- ========================================================================
-- Note: The 'anon' user is used by Turbine for anonymous/unauthenticated sessions.
--       This user should exist in the database for the application to work properly.
-- ========================================================================

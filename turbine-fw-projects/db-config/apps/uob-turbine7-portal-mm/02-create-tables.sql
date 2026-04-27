-- ========================================================================
-- UOB Turbine7 Portal MM - Table Creation Script
-- ========================================================================
-- This script creates all database tables with proper structure
-- Run this as kishore user:
--   mysql -u kishore -pKish1381@ kishore < 02-create-tables.sql
-- ========================================================================
-- Tables Created:
--   1. GTP_USER - User accounts
--   2. GTP_ROLE - Security roles
--   3. GTP_PERMISSION - Security permissions
--   4. GTP_GROUP - User groups
--   5. GTP_ROLE_PERMISSION - Maps roles to permissions
--   6. GTP_GROUP_ROLE - Maps groups to roles
--   7. GTP_USER_GROUP_ROLE - Maps users to groups and roles
-- ========================================================================

USE kishore;

SET FOREIGN_KEY_CHECKS = 0;

-- ========================================================================
-- Drop existing tables if they exist (for clean setup)
-- ========================================================================
DROP TABLE IF EXISTS GTP_USER_GROUP_ROLE;
DROP TABLE IF EXISTS GTP_GROUP_ROLE;
DROP TABLE IF EXISTS GTP_ROLE_PERMISSION;
DROP TABLE IF EXISTS GTP_USER;
DROP TABLE IF EXISTS GTP_GROUP;
DROP TABLE IF EXISTS GTP_ROLE;
DROP TABLE IF EXISTS GTP_PERMISSION;

-- ========================================================================
-- 1. GTP_PERMISSION - Security Permissions
-- ========================================================================
CREATE TABLE GTP_PERMISSION (
    PERMISSION_ID INT NOT NULL AUTO_INCREMENT,
    PERMISSION_NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (PERMISSION_ID),
    UNIQUE KEY UK_PERMISSION_NAME (PERMISSION_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

-- ========================================================================
-- 2. GTP_ROLE - Security Roles
-- ========================================================================
CREATE TABLE GTP_ROLE (
    ROLE_ID INT NOT NULL AUTO_INCREMENT,
    ROLE_NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (ROLE_ID),
    UNIQUE KEY UK_ROLE_NAME (ROLE_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

-- ========================================================================
-- 3. GTP_GROUP - User Groups
-- ========================================================================
CREATE TABLE GTP_GROUP (
    GROUP_ID INT NOT NULL AUTO_INCREMENT,
    GROUP_NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (GROUP_ID),
    UNIQUE KEY UK_GROUP_NAME (GROUP_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

-- ========================================================================
-- 4. GTP_USER - User Accounts
-- ========================================================================
CREATE TABLE GTP_USER (
    USER_ID INT NOT NULL AUTO_INCREMENT,
    LOGIN_NAME VARCHAR(255) NOT NULL,
    PASSWORD_VALUE VARCHAR(255) NOT NULL,
    FIRST_NAME VARCHAR(255),
    LAST_NAME VARCHAR(255),
    EMAIL VARCHAR(255),
    CONFIRM_VALUE VARCHAR(255),
    CREATED DATETIME DEFAULT CURRENT_TIMESTAMP,
    MODIFIED_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    LAST_LOGIN DATETIME,
    OBJECTDATA TEXT,
    PRIMARY KEY (USER_ID),
    UNIQUE KEY UK_LOGIN_NAME (LOGIN_NAME),
    KEY IDX_EMAIL (EMAIL),
    KEY IDX_CREATED (CREATED)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
AUTO_INCREMENT=1;

-- ========================================================================
-- 5. GTP_ROLE_PERMISSION - Maps Roles to Permissions (Many-to-Many)
-- ========================================================================
CREATE TABLE GTP_ROLE_PERMISSION (
    ROLE_ID INT NOT NULL,
    PERMISSION_ID INT NOT NULL,
    PRIMARY KEY (ROLE_ID, PERMISSION_ID),
    CONSTRAINT FK_ROLE_PERMISSION_ROLE FOREIGN KEY (ROLE_ID) 
        REFERENCES GTP_ROLE (ROLE_ID) ON DELETE CASCADE,
    CONSTRAINT FK_ROLE_PERMISSION_PERMISSION FOREIGN KEY (PERMISSION_ID) 
        REFERENCES GTP_PERMISSION (PERMISSION_ID) ON DELETE CASCADE,
    KEY IDX_ROLE_ID (ROLE_ID),
    KEY IDX_PERMISSION_ID (PERMISSION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================================
-- 6. GTP_GROUP_ROLE - Maps Groups to Roles (Many-to-Many)
-- ========================================================================
CREATE TABLE GTP_GROUP_ROLE (
    GROUP_ID INT NOT NULL,
    ROLE_ID INT NOT NULL,
    PRIMARY KEY (GROUP_ID, ROLE_ID),
    CONSTRAINT FK_GROUP_ROLE_GROUP FOREIGN KEY (GROUP_ID) 
        REFERENCES GTP_GROUP (GROUP_ID) ON DELETE CASCADE,
    CONSTRAINT FK_GROUP_ROLE_ROLE FOREIGN KEY (ROLE_ID) 
        REFERENCES GTP_ROLE (ROLE_ID) ON DELETE CASCADE,
    KEY IDX_GROUP_ID (GROUP_ID),
    KEY IDX_ROLE_ID (ROLE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================================
-- 7. GTP_USER_GROUP_ROLE - Maps Users to Groups and Roles (Many-to-Many)
-- ========================================================================
CREATE TABLE GTP_USER_GROUP_ROLE (
    USER_ID INT NOT NULL,
    GROUP_ID INT NOT NULL,
    ROLE_ID INT NOT NULL,
    PRIMARY KEY (USER_ID, GROUP_ID, ROLE_ID),
    CONSTRAINT FK_USER_GROUP_ROLE_USER FOREIGN KEY (USER_ID) 
        REFERENCES GTP_USER (USER_ID) ON DELETE CASCADE,
    CONSTRAINT FK_USER_GROUP_ROLE_GROUP FOREIGN KEY (GROUP_ID) 
        REFERENCES GTP_GROUP (GROUP_ID) ON DELETE CASCADE,
    CONSTRAINT FK_USER_GROUP_ROLE_ROLE FOREIGN KEY (ROLE_ID) 
        REFERENCES GTP_ROLE (ROLE_ID) ON DELETE CASCADE,
    KEY IDX_USER_ID (USER_ID),
    KEY IDX_GROUP_ID (GROUP_ID),
    KEY IDX_ROLE_ID (ROLE_ID),
    KEY IDX_USER_GROUP (USER_ID, GROUP_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================================
-- Verification
-- ========================================================================
SELECT 'All tables created successfully!' AS Status;
SHOW TABLES;

-- Display table structures
DESCRIBE GTP_USER;
DESCRIBE GTP_ROLE;
DESCRIBE GTP_PERMISSION;
DESCRIBE GTP_GROUP;
DESCRIBE GTP_ROLE_PERMISSION;
DESCRIBE GTP_GROUP_ROLE;
DESCRIBE GTP_USER_GROUP_ROLE;

-- ========================================================================
-- Table Creation Summary
-- ========================================================================
-- ✓ GTP_PERMISSION - Security permissions
-- ✓ GTP_ROLE - Security roles
-- ✓ GTP_GROUP - User groups
-- ✓ GTP_USER - User accounts
-- ✓ GTP_ROLE_PERMISSION - Role-Permission mapping
-- ✓ GTP_GROUP_ROLE - Group-Role mapping
-- ✓ GTP_USER_GROUP_ROLE - User-Group-Role mapping
-- ========================================================================

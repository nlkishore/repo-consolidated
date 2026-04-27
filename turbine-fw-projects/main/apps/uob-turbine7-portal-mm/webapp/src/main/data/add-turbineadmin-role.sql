-- ========================================================================
-- Add turbineadmin Role for Turbine/Flux Compatibility
-- ========================================================================
-- This script adds a "turbineadmin" role to the database and assigns it
-- to users who have the "ADMIN" role. This ensures compatibility with
-- Turbine's Flux admin screens which expect "turbineadmin" role.
-- ========================================================================

USE kishore;

SET FOREIGN_KEY_CHECKS = 0;

-- ========================================================================
-- Step 1: Add turbineadmin role if it doesn't exist
-- ========================================================================
INSERT IGNORE INTO GTP_ROLE (ROLE_ID, ROLE_NAME) VALUES
(5, 'turbineadmin');

-- ========================================================================
-- Step 2: Map turbineadmin role to ADMINISTRATORS group
-- ========================================================================
-- Add turbineadmin role to ADMINISTRATORS group (same as ADMIN role)
INSERT IGNORE INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID) 
SELECT 1, 5  -- ADMINISTRATORS group, turbineadmin role
WHERE EXISTS (SELECT 1 FROM GTP_GROUP WHERE GROUP_ID = 1);

-- ========================================================================
-- Step 3: Assign turbineadmin role to all users who have ADMIN role
-- ========================================================================
-- Find all users with ADMIN role and also assign them turbineadmin role
INSERT IGNORE INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
SELECT DISTINCT ugr.USER_ID, 1, 5  -- ADMINISTRATORS group, turbineadmin role
FROM GTP_USER_GROUP_ROLE ugr
INNER JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
WHERE r.ROLE_NAME = 'ADMIN'
  AND ugr.GROUP_ID = 1  -- ADMINISTRATORS group
  AND NOT EXISTS (
    SELECT 1 FROM GTP_USER_GROUP_ROLE ugr2
    WHERE ugr2.USER_ID = ugr.USER_ID
      AND ugr2.GROUP_ID = 1
      AND ugr2.ROLE_ID = 5
  );

-- ========================================================================
-- Step 4: Grant all permissions to turbineadmin role (same as ADMIN)
-- ========================================================================
-- Copy all permissions from ADMIN role to turbineadmin role
INSERT IGNORE INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT 5, rp.PERMISSION_ID  -- turbineadmin role gets all ADMIN permissions
FROM GTP_ROLE_PERMISSION rp
INNER JOIN GTP_ROLE r ON rp.ROLE_ID = r.ROLE_ID
WHERE r.ROLE_NAME = 'ADMIN';

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================================
-- Verification Query
-- ========================================================================
-- Run this to verify the setup:
-- SELECT u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME
-- FROM GTP_USER u
-- INNER JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
-- INNER JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
-- INNER JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
-- WHERE r.ROLE_NAME IN ('ADMIN', 'turbineadmin')
-- ORDER BY u.LOGIN_NAME, r.ROLE_NAME;

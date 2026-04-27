-- ========================================================================
-- UOB Turbine7 Portal MM - Oracle Schema and User Creation Script
-- ========================================================================
-- This script creates the schema and user account for the application
-- Run this as SYSDBA:
--   sqlplus sys/password@localhost:1521/ORCL as sysdba @01-create-schema-and-user.sql
-- ========================================================================
-- Schema/User: KISHORE
-- Password: Kish1381@
-- Tablespace: USERS (default)
-- ========================================================================

-- Connect as SYSDBA (this script should be run as SYSDBA)
-- The user running this script must have SYSDBA or DBA privileges

-- ========================================================================
-- Step 1: Drop existing user if it exists (optional - comment out if you want to keep existing data)
-- ========================================================================
-- WARNING: This will drop the user and all objects owned by the user
-- Uncomment the following lines if you want to start fresh:
-- DROP USER KISHORE CASCADE;

-- ========================================================================
-- Step 2: Create the user/schema
-- ========================================================================
CREATE USER KISHORE IDENTIFIED BY "Kish1381@"
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
QUOTA UNLIMITED ON USERS;

-- ========================================================================
-- Step 3: Grant necessary privileges
-- ========================================================================
-- Basic privileges for application
GRANT CONNECT TO KISHORE;
GRANT RESOURCE TO KISHORE;
GRANT CREATE SESSION TO KISHORE;
GRANT CREATE TABLE TO KISHORE;
GRANT CREATE SEQUENCE TO KISHORE;
GRANT CREATE VIEW TO KISHORE;
GRANT CREATE PROCEDURE TO KISHORE;
GRANT CREATE TRIGGER TO KISHORE;

-- Additional privileges that may be needed
GRANT UNLIMITED TABLESPACE TO KISHORE;

-- ========================================================================
-- Step 4: Verify user creation
-- ========================================================================
SELECT 'User KISHORE created successfully!' AS Status FROM DUAL;

SELECT username, account_status, default_tablespace, temporary_tablespace
FROM dba_users
WHERE username = 'KISHORE';

-- Show granted privileges
SELECT privilege
FROM dba_sys_privs
WHERE grantee = 'KISHORE'
ORDER BY privilege;

-- ========================================================================
-- Verification
-- ========================================================================
-- After running this script, verify:
-- 1. User exists: SELECT username FROM all_users WHERE username = 'KISHORE';
-- 2. Can connect: sqlplus kishore/Kish1381@localhost:1521/ORCL
-- ========================================================================

COMMIT;

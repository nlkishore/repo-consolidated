-- ========================================================================
-- UOB Turbine7 Portal MM - Database and User Creation Script
-- ========================================================================
-- This script creates the database and user account for the application
-- Run this as MySQL root user:
--   mysql -u root -p < 01-create-database-and-user.sql
-- ========================================================================
-- Database: kishore
-- User: kishore
-- Password: Kish1381@
-- ========================================================================

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS kishore
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Use the database
USE kishore;

-- Create the user if it doesn't exist
CREATE USER IF NOT EXISTS 'kishore'@'localhost' IDENTIFIED BY 'Kish1381@';

-- Grant all privileges on the kishore database
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost';

-- Grant privileges with grant option (allows user to grant privileges to others)
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost' WITH GRANT OPTION;

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Verify the user was created
SELECT 'Database and user creation completed successfully!' AS Status;
SELECT User, Host FROM mysql.user WHERE User='kishore';

-- Show current privileges
SHOW GRANTS FOR 'kishore'@'localhost';

-- ========================================================================
-- Verification
-- ========================================================================
-- After running this script, verify:
-- 1. Database exists: SHOW DATABASES LIKE 'kishore';
-- 2. User exists: SELECT User, Host FROM mysql.user WHERE User='kishore';
-- 3. Can connect: mysql -u kishore -pKish1381@ kishore
-- ========================================================================

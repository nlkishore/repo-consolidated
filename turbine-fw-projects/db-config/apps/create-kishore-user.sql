-- ============================================
-- MySQL User Creation Script for kishore
-- ============================================
-- This script creates the 'kishore' user and grants privileges
-- Run this as MySQL root user:
-- mysql -u root -p < create-kishore-user.sql
-- ============================================

-- Create the user if it doesn't exist
CREATE USER IF NOT EXISTS 'kishore'@'localhost' IDENTIFIED BY 'Kish1381@';

-- Grant all privileges on the kishore database
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost';

-- Also grant privileges for any existing database
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost' WITH GRANT OPTION;

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Verify the user was created
SELECT User, Host FROM mysql.user WHERE User='kishore';

-- Show current privileges
SHOW GRANTS FOR 'kishore'@'localhost';

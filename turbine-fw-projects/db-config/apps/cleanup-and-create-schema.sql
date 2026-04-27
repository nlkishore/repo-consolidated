-- ============================================
-- MySQL Schema Cleanup and Creation Script
-- ============================================
-- This script will:
-- 1. Drop the 'kishore' schema if it exists
-- 2. Create a fresh 'kishore' schema
-- 3. Optionally clean up other user schemas
-- ============================================

-- Show current databases
SHOW DATABASES;

-- Drop kishore schema if it exists (this will delete all tables and data)
DROP DATABASE IF EXISTS kishore;

-- Create fresh kishore schema
CREATE DATABASE kishore 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Verify creation
SHOW DATABASES LIKE 'kishore';

-- Use the new schema
USE kishore;

-- Show current database
SELECT DATABASE();

-- Display success message
SELECT 'Schema kishore created successfully!' AS Status;

-- ============================================
-- MySQL Schema Cleanup Script (Comprehensive)
-- ============================================
-- This script will drop all user-created schemas
-- and keep only system schemas:
-- - information_schema
-- - mysql
-- - performance_schema
-- - sys
-- ============================================

-- Show all databases
SHOW DATABASES;

-- Drop user-created schemas (excluding system schemas)
-- Add your schema names here that you want to drop

-- Example: Drop common user schemas
DROP DATABASE IF EXISTS kishore;
DROP DATABASE IF EXISTS turbine;
DROP DATABASE IF EXISTS test;
DROP DATABASE IF EXISTS sample;

-- Create fresh kishore schema
CREATE DATABASE IF NOT EXISTS kishore 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;

SELECT 'Cleanup completed. Fresh kishore schema created!' AS Status;

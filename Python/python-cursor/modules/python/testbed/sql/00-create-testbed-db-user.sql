-- Create dedicated testbed schema and local dev user (no password).
-- Requires kishore (or other account with CREATE USER + GRANT OPTION).

CREATE DATABASE IF NOT EXISTS testbed
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

DROP USER IF EXISTS 'testbed'@'localhost';
CREATE USER 'testbed'@'localhost' IDENTIFIED BY '';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER
  ON testbed.* TO 'testbed'@'localhost';

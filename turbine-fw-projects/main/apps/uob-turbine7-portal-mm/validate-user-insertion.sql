-- SQL Queries to Validate User Insertion
-- Run these queries in MySQL Workbench or command line

USE kishore;

-- 1. Check all users (most recent first)
SELECT 
    USER_ID, 
    LOGIN_NAME, 
    FIRST_NAME, 
    LAST_NAME, 
    EMAIL, 
    CREATED,
    MODIFIED_DATE,
    LAST_LOGIN
FROM GTP_USER 
ORDER BY USER_ID DESC 
LIMIT 20;

-- 2. Check recent user insertions (last hour)
SELECT 
    USER_ID, 
    LOGIN_NAME, 
    FIRST_NAME, 
    LAST_NAME, 
    EMAIL, 
    CREATED,
    MODIFIED_DATE
FROM GTP_USER 
WHERE CREATED >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY CREATED DESC;

-- 3. Check total user count
SELECT COUNT(*) as total_users FROM GTP_USER;

-- 4. Check if a specific user exists
-- Replace 'testuser' with the username you created
SELECT 
    USER_ID, 
    LOGIN_NAME, 
    FIRST_NAME, 
    LAST_NAME, 
    EMAIL, 
    CREATED
FROM GTP_USER 
WHERE LOGIN_NAME = 'testuser';

-- 5. Check user with group and role assignments
SELECT 
    u.USER_ID,
    u.LOGIN_NAME,
    u.FIRST_NAME,
    u.LAST_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME
FROM GTP_USER u
LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
ORDER BY u.USER_ID DESC, u.LOGIN_NAME;

-- 6. Check for users without group/role assignments
SELECT 
    u.USER_ID,
    u.LOGIN_NAME,
    u.FIRST_NAME,
    u.LAST_NAME,
    u.EMAIL,
    u.CREATED
FROM GTP_USER u
LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
WHERE ugr.USER_ID IS NULL
ORDER BY u.USER_ID DESC;

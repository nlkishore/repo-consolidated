@echo off
REM ============================================
REM MySQL Schema Cleanup and Creation Script
REM ============================================
REM This script will:
REM 1. Connect to MySQL
REM 2. Drop the 'kishore' schema if it exists
REM 3. Create a fresh 'kishore' schema
REM ============================================

echo ============================================
echo MySQL Schema Cleanup and Creation
echo ============================================
echo.

REM Set MySQL paths
set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe"
set MYSQL_USER=root
set MYSQL_PASSWORD=Kish1381@

REM Check if MySQL executable exists
if not exist %MYSQL_BIN% (
    echo ERROR: MySQL executable not found at %MYSQL_BIN%
    echo Please update the MYSQL_BIN path in this script.
    pause
    exit /b 1
)

echo Step 1: Showing current databases...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "SHOW DATABASES;"
echo.

echo Step 2: Dropping kishore schema if it exists...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "DROP DATABASE IF EXISTS kishore;"
echo.

echo Step 3: Creating fresh kishore schema...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "CREATE DATABASE kishore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo.

echo Step 4: Creating kishore user if not exists...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "CREATE USER IF NOT EXISTS 'kishore'@'localhost' IDENTIFIED BY 'Kish1381@';"
echo.

echo Step 5: Granting privileges to kishore user...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost';"
echo.

echo Step 6: Flushing privileges...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "FLUSH PRIVILEGES;"
echo.

echo Step 7: Verifying creation...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "SHOW DATABASES LIKE 'kishore';"
echo.

echo Step 8: Verifying user...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "SELECT User, Host FROM mysql.user WHERE User='kishore';"
echo.

echo Step 9: Showing database details...
%MYSQL_BIN% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "USE kishore; SHOW CREATE DATABASE kishore;"
echo.

echo ============================================
echo SUCCESS: Fresh kishore schema created!
echo ============================================
pause

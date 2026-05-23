@echo off
REM Load testbed environment for current CMD window.
REM Usage:  call scripts\env-testbed.bat

cd /d "%~dp0.."
set "TESTBED_ROOT=%CD%"

set "DB_HOST=localhost"
set "DB_NAME=testbed"
set "DB_USER=testbed"
set "DB_PASSWORD="

set "MYSQL_HOME=C:\Program Files\MySQL\MySQL Server 8.1"
set "MYSQL_BIN=%MYSQL_HOME%\bin"
set "MYSQL_EXE=%MYSQL_BIN%\mysql.exe"
set "PATH=%MYSQL_BIN%;%PATH%"

echo Testbed env loaded.
echo   DB: %DB_USER%@%DB_HOST%/%DB_NAME% (no password)
echo   Root: %TESTBED_ROOT%

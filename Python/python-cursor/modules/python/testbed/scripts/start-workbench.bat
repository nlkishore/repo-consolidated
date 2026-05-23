@echo off
REM Launch MySQL Workbench (optional GUI).
set "WB=C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe"
if exist "%WB%" (
    start "" "%WB%"
    echo Launched MySQL Workbench.
    echo Connect: localhost:3306  user=testbed  password=(empty)  schema=testbed
) else (
    echo MySQL Workbench not found at: %WB%
    exit /b 1
)

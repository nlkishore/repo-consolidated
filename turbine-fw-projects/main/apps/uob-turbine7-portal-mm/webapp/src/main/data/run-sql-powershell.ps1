# ========================================================================
# PowerShell Script to Run SQL File (PowerShell-Compatible)
# ========================================================================
# This script properly executes SQL files in PowerShell
# ========================================================================

$ErrorActionPreference = "Stop"

# Database configuration
$dbUser = "kishore"
$dbPassword = "Kish1381@"
$dbName = "kishore"
$sqlFile = "assign-roles-to-all-users.sql"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Assigning Roles to All Users" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if SQL file exists
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found: $sqlFile" -ForegroundColor Red
    Write-Host "Current directory: $(Get-Location)" -ForegroundColor Yellow
    Write-Host "Please navigate to: C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\data" -ForegroundColor Yellow
    exit 1
}

Write-Host "SQL file found: $sqlFile" -ForegroundColor Green
Write-Host ""

# Find MySQL executable
$mysqlExe = $null
$mysqlPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.2\bin\mysql.exe",
    "C:\mysql\bin\mysql.exe",
    "C:\mysql81\bin\mysql.exe"
)

foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysqlExe = $path
        Write-Host "MySQL found at: $mysqlExe" -ForegroundColor Green
        break
    }
}

# Try PATH
if ($null -eq $mysqlExe) {
    try {
        $test = Get-Command mysql -ErrorAction SilentlyContinue
        if ($test) {
            $mysqlExe = "mysql"
            Write-Host "MySQL found in PATH" -ForegroundColor Green
        }
    } catch {
        # Not in PATH
    }
}

# If not found, suggest MySQL Workbench
if ($null -eq $mysqlExe) {
    Write-Host "WARNING: MySQL command line tool not found." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please use MySQL Workbench instead:" -ForegroundColor Cyan
    Write-Host "1. Open: C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe" -ForegroundColor White
    Write-Host "2. Connect to database 'kishore'" -ForegroundColor White
    Write-Host "3. Open file: $(Resolve-Path $sqlFile)" -ForegroundColor White
    Write-Host "4. Execute the script" -ForegroundColor White
    Write-Host ""
    exit 0
}

# Execute SQL script using PowerShell-compatible method
Write-Host "Executing SQL script..." -ForegroundColor Yellow
try {
    # Read SQL file content
    $sqlContent = Get-Content $sqlFile -Raw -Encoding UTF8
    
    # Execute using PowerShell pipe (PowerShell-compatible)
    if ($mysqlExe -eq "mysql") {
        $result = $sqlContent | & mysql -u $dbUser -p"$dbPassword" $dbName 2>&1
    } else {
        $result = $sqlContent | & $mysqlExe -u $dbUser -p"$dbPassword" $dbName 2>&1
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "SUCCESS: Roles assigned to all users!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Verification query results:" -ForegroundColor Cyan
        Write-Host "===========================" -ForegroundColor Cyan
        
        # Run verification query
        $verifyQuery = @"
SELECT 
    u.LOGIN_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME,
    COUNT(DISTINCT p.PERMISSION_ID) as PERMISSION_COUNT
FROM GTP_USER u
LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
LEFT JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
LEFT JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
GROUP BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME
ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME;
"@
        
        if ($mysqlExe -eq "mysql") {
            $verifyQuery | & mysql -u $dbUser -p"$dbPassword" $dbName
        } else {
            $verifyQuery | & $mysqlExe -u $dbUser -p"$dbPassword" $dbName
        }
        
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "Next Steps:" -ForegroundColor Yellow
        Write-Host "1. Restart Tomcat if it's running" -ForegroundColor White
        Write-Host "2. Login to the application" -ForegroundColor White
        Write-Host "3. Navigate to UserProfile page" -ForegroundColor White
        Write-Host "4. Verify roles, groups, and permissions are displayed" -ForegroundColor White
        Write-Host "========================================" -ForegroundColor Cyan
        
    } else {
        Write-Host "ERROR: SQL execution failed" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "ERROR: Failed to execute SQL script" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Try using MySQL Workbench instead (see instructions above)" -ForegroundColor Yellow
    exit 1
}

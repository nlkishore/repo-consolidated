# ========================================================================
# Fixed PowerShell Script to Run SQL File
# ========================================================================
# Uses MySQL's --source option for reliable execution
# ========================================================================

$ErrorActionPreference = "Continue"

# Database configuration
$dbUser = "kishore"
$dbPassword = "Kish1381@"
$dbName = "kishore"
$sqlFile = "assign-roles-to-all-users.sql"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Assigning Roles to All Users" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find MySQL executable
$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe"

if (-not (Test-Path $mysqlExe)) {
    Write-Host "ERROR: MySQL not found at: $mysqlExe" -ForegroundColor Red
    Write-Host "Please use MySQL Workbench instead." -ForegroundColor Yellow
    exit 1
}

Write-Host "MySQL found: $mysqlExe" -ForegroundColor Green

# Check SQL file
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found: $sqlFile" -ForegroundColor Red
    Write-Host "Current directory: $(Get-Location)" -ForegroundColor Yellow
    exit 1
}

Write-Host "SQL file: $sqlFile" -ForegroundColor Green
Write-Host ""

# Get absolute path to SQL file
$sqlFilePath = (Resolve-Path $sqlFile).Path

Write-Host "Executing SQL script..." -ForegroundColor Yellow
Write-Host ""

# Use MySQL's source command via --execute
# First, change to the directory and use source command
$sourceCommand = "source $sqlFilePath"

# Execute using --execute option
$result = & $mysqlExe -u $dbUser -p"$dbPassword" $dbName --execute $sourceCommand 2>&1

# Filter out warnings
$output = $result | Where-Object { 
    $_ -notmatch "Warning" -and 
    $_ -notmatch "Using a password" -and 
    $_ -ne ""
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS: SQL script executed!" -ForegroundColor Green
    if ($output.Count -gt 0) {
        Write-Host ""
        Write-Host "Output:" -ForegroundColor Cyan
        $output | ForEach-Object { Write-Host $_ -ForegroundColor White }
    }
} else {
    Write-Host "ERROR: SQL execution failed" -ForegroundColor Red
    Write-Host "Exit code: $LASTEXITCODE" -ForegroundColor Red
    if ($output.Count -gt 0) {
        Write-Host "Errors:" -ForegroundColor Red
        $output | ForEach-Object { Write-Host $_ -ForegroundColor Red }
    }
    exit 1
}

Write-Host ""
Write-Host "Verifying results..." -ForegroundColor Cyan
Write-Host "===========================" -ForegroundColor Cyan

$verifyQuery = @"
SELECT 
    u.LOGIN_NAME,
    g.GROUP_NAME,
    r.ROLE_NAME,
    COUNT(DISTINCT p.PERMISSION_ID) as PERMISSIONS
FROM GTP_USER u
LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
LEFT JOIN GTP_ROLE_PERMISSION rp ON r.ROLE_ID = rp.ROLE_ID
LEFT JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
GROUP BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME
ORDER BY u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME;
"@

$verifyResult = & $mysqlExe -u $dbUser -p"$dbPassword" $dbName -e $verifyQuery 2>&1 | Where-Object { 
    $_ -notmatch "Warning" -and 
    $_ -notmatch "Using a password"
}

$verifyResult | ForEach-Object { Write-Host $_ -ForegroundColor White }

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Restart Tomcat if it's running" -ForegroundColor White
Write-Host "2. Login to the application" -ForegroundColor White
Write-Host "3. Navigate to UserProfile page" -ForegroundColor White
Write-Host "4. Verify roles, groups, and permissions are displayed" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

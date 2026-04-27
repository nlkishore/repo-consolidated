# ========================================================================
# PowerShell Script to Assign Roles to All Users
# ========================================================================
# This script executes the SQL to assign roles, groups, and permissions
# to all existing users in the database.
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

# Check if MySQL command line is available
$mysqlExe = $null
$mysqlPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.2\bin\mysql.exe",
    "C:\mysql\bin\mysql.exe",
    "C:\mysql81\bin\mysql.exe"
)

# Try to find mysql.exe
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysqlExe = $path
        Write-Host "MySQL found at: $mysqlExe" -ForegroundColor Green
        break
    }
}

# If not found in common paths, try PATH
if ($null -eq $mysqlExe) {
    try {
        $mysqlVersion = mysql --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            $mysqlExe = "mysql"
            Write-Host "MySQL found in PATH: $mysqlVersion" -ForegroundColor Green
        }
    } catch {
        # MySQL not in PATH
    }
}

# If still not found, provide instructions
if ($null -eq $mysqlExe) {
    Write-Host "WARNING: MySQL command line tool not found." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "You can use MySQL Workbench instead:" -ForegroundColor Cyan
    Write-Host "1. Open: C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe" -ForegroundColor White
    Write-Host "2. Connect to database 'kishore' with user 'kishore'" -ForegroundColor White
    Write-Host "3. Open file: $sqlFile" -ForegroundColor White
    Write-Host "4. Execute the script" -ForegroundColor White
    Write-Host ""
    Write-Host "Or install MySQL Server to use command line tools." -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "Do you want to continue anyway? (y/n)"
    if ($continue -ne "y" -and $continue -ne "Y") {
        exit 0
    }
    Write-Host ""
    Write-Host "Please run the SQL script manually using MySQL Workbench." -ForegroundColor Yellow
    Write-Host "SQL file location: $(Resolve-Path $sqlFile)" -ForegroundColor Cyan
    exit 0
}

# Check if SQL file exists
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found: $sqlFile" -ForegroundColor Red
    Write-Host "Current directory: $(Get-Location)" -ForegroundColor Yellow
    exit 1
}

Write-Host "SQL file found: $sqlFile" -ForegroundColor Green
Write-Host ""

# Execute SQL script
Write-Host "Executing SQL script..." -ForegroundColor Yellow
try {
    # Read SQL file content
    $sqlContent = Get-Content $sqlFile -Raw -Encoding UTF8
    
    # Create a temporary file for MySQL to read (avoids encoding/pipe issues)
    $tempFile = [System.IO.Path]::GetTempFileName()
    $sqlContent | Out-File -FilePath $tempFile -Encoding UTF8 -NoNewline
    
    try {
        # Execute using source command or input file
        if ($mysqlExe -eq "mysql") {
            # Use --defaults-extra-file to avoid password warning, or use input redirection
            $result = Get-Content $tempFile | & mysql -u $dbUser -p"$dbPassword" $dbName 2>&1
        } else {
            $result = Get-Content $tempFile | & $mysqlExe -u $dbUser -p"$dbPassword" $dbName 2>&1
        }
        
        # Check exit code
        $exitCode = $LASTEXITCODE
        
        # Filter out password warnings
        $errors = $result | Where-Object { $_ -notmatch "Using a password" -and $_ -notmatch "Warning" }
        
        if ($exitCode -eq 0 -or ($errors.Count -eq 0)) {
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
            $verifyQuery | mysql -u $dbUser -p"$dbPassword" $dbName
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
            Write-Host "Exit code: $exitCode" -ForegroundColor Red
            if ($errors.Count -gt 0) {
                Write-Host "Errors:" -ForegroundColor Red
                $errors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
            } else {
                Write-Host "Output:" -ForegroundColor Yellow
                $result | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }
            }
            Remove-Item $tempFile -ErrorAction SilentlyContinue
            exit 1
        }
    } finally {
        # Clean up temp file
        Remove-Item $tempFile -ErrorAction SilentlyContinue
    }
} catch {
    Write-Host "ERROR: Failed to execute SQL script" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host $_.Exception.StackTrace -ForegroundColor Gray
    exit 1
}

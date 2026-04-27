# ========================================================================
# Simple PowerShell Script to Run SQL File
# ========================================================================
# This version uses a simpler approach with better error handling
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

if ($null -eq $mysqlExe) {
    Write-Host "ERROR: MySQL executable not found." -ForegroundColor Red
    Write-Host "Please use MySQL Workbench instead." -ForegroundColor Yellow
    Write-Host "Location: C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe" -ForegroundColor Cyan
    exit 1
}

# Check SQL file
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "SQL file: $sqlFile" -ForegroundColor Green
Write-Host ""

# Execute SQL using --execute option
Write-Host "Executing SQL script..." -ForegroundColor Yellow

# Read SQL content and replace line breaks with spaces for --execute
$sqlContent = Get-Content $sqlFile -Raw -Encoding UTF8
# Remove comments and clean up
$sqlContent = $sqlContent -replace '--.*', '' -replace '/\*.*?\*/', '' -replace '\s+', ' '

# Split by semicolons and execute each statement
$statements = $sqlContent -split ';' | Where-Object { $_.Trim() -ne '' -and $_.Trim() -ne 'USE kishore' }

$successCount = 0
$errorCount = 0

foreach ($statement in $statements) {
    $stmt = $statement.Trim()
    if ($stmt.Length -gt 0) {
        try {
            $result = & $mysqlExe -u $dbUser -p"$dbPassword" $dbName -e $stmt 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "Using a password" }
            if ($LASTEXITCODE -eq 0) {
                $successCount++
            } else {
                $errorCount++
                Write-Host "Error executing statement:" -ForegroundColor Red
                Write-Host $stmt.Substring(0, [Math]::Min(100, $stmt.Length)) -ForegroundColor Gray
            }
        } catch {
            $errorCount++
            Write-Host "Exception: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
if ($errorCount -eq 0) {
    Write-Host "SUCCESS: All SQL statements executed!" -ForegroundColor Green
    Write-Host "Statements executed: $successCount" -ForegroundColor Green
} else {
    Write-Host "WARNING: Some statements failed" -ForegroundColor Yellow
    Write-Host "Successful: $successCount, Failed: $errorCount" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Verifying results..." -ForegroundColor Cyan

$verifyQuery = "SELECT u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME FROM GTP_USER u LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID ORDER BY u.LOGIN_NAME;"

& $mysqlExe -u $dbUser -p"$dbPassword" $dbName -e $verifyQuery 2>&1 | Where-Object { $_ -notmatch "Warning" -and $_ -notmatch "Using a password" }

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Restart Tomcat" -ForegroundColor White
Write-Host "2. Login to application" -ForegroundColor White
Write-Host "3. Check UserProfile page" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

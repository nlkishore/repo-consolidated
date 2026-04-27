# ========================================================================
# Load Test Data into GTP Tables
# ========================================================================
# This script loads test data into the kishore database
# ========================================================================

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Loading Test Data into GTP Tables" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Database configuration
$dbUser = "kishore"
$dbPassword = "Kish1381@"
$dbName = "kishore"
$sqlFile = Join-Path $PSScriptRoot "gtp-test-data.sql"

# Check if SQL file exists
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "SQL File: $sqlFile" -ForegroundColor Yellow
Write-Host "Database: $dbName" -ForegroundColor Yellow
Write-Host "User: $dbUser" -ForegroundColor Yellow
Write-Host ""

# Try to find MySQL executable
$mysqlPath = $null
$possiblePaths = @(
    "mysql",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe",
    "C:\mysql\bin\mysql.exe",
    "C:\mysql81\bin\mysql.exe"
)

foreach ($path in $possiblePaths) {
    if (Get-Command $path -ErrorAction SilentlyContinue) {
        $mysqlPath = $path
        break
    }
}

if ($null -eq $mysqlPath) {
    Write-Host "ERROR: MySQL executable not found in PATH." -ForegroundColor Red
    Write-Host ""
    Write-Host "Please either:" -ForegroundColor Yellow
    Write-Host "  1. Add MySQL to your PATH, or" -ForegroundColor Yellow
    Write-Host "  2. Edit this script and set mysqlPath to your MySQL executable path" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Example MySQL paths:" -ForegroundColor Yellow
    foreach ($path in $possiblePaths) {
        Write-Host "  - $path" -ForegroundColor Gray
    }
    exit 1
}

Write-Host "Using MySQL: $mysqlPath" -ForegroundColor Green
Write-Host ""

# Execute SQL script
Write-Host "Loading test data..." -ForegroundColor Yellow
try {
    $sqlContent = Get-Content $sqlFile -Raw
    $sqlContent | & $mysqlPath -u $dbUser "-p$dbPassword" $dbName
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Test data loaded successfully!" -ForegroundColor Green
        Write-Host ""
        
        # Verify data
        Write-Host "Verifying data..." -ForegroundColor Yellow
        $verifyQueries = @(
            "SELECT COUNT(*) as 'User Count' FROM GTP_USER;",
            "SELECT COUNT(*) as 'Role Count' FROM GTP_ROLE;",
            "SELECT COUNT(*) as 'Permission Count' FROM GTP_PERMISSION;",
            "SELECT COUNT(*) as 'Group Count' FROM GTP_GROUP;"
        )
        
        foreach ($query in $verifyQueries) {
            & $mysqlPath -u $dbUser "-p$dbPassword" $dbName -e $query
        }
        
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "Test Data Summary" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Test Users:" -ForegroundColor Yellow
        Write-Host "  - admin (password: password123) - ADMIN role" -ForegroundColor White
        Write-Host "  - manager1 (password: password123) - MANAGER role" -ForegroundColor White
        Write-Host "  - manager2 (password: password123) - MANAGER role" -ForegroundColor White
        Write-Host "  - user1 (password: password123) - USER role" -ForegroundColor White
        Write-Host "  - user2 (password: password123) - USER role" -ForegroundColor White
        Write-Host "  - anon (password: anon) - ANONYMOUS role" -ForegroundColor White
        Write-Host ""
        Write-Host "Access the application at:" -ForegroundColor Yellow
        Write-Host "  http://localhost:8081/uob-t7-portal-mm-tomcat/app" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "Error loading test data. Exit code: $LASTEXITCODE" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "Error executing SQL script:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

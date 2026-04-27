# ========================================================================
# UOB Turbine7 Portal MM - Automated Database Setup Script (PowerShell)
# ========================================================================
# This script automates the complete database setup process:
#   1. Creates database and user
#   2. Creates all tables
#   3. Loads test data
#   4. Verifies setup
# ========================================================================
# Usage:
#   .\setup-database.ps1
# ========================================================================
# Requirements:
#   - MySQL 8.1 installed and running
#   - MySQL root access
#   - PowerShell 5.1 or later
# ========================================================================

param(
    [string]$RootPassword = "",
    [string]$DatabaseName = "kishore",
    [string]$DatabaseUser = "kishore",
    [string]$DatabasePassword = "Kish1381@",
    [string]$MySQLPath = "mysql"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "UOB Turbine7 Portal MM - Database Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get script directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# Check if MySQL is available
Write-Host "Checking MySQL installation..." -ForegroundColor Yellow
try {
    $mysqlVersion = & $MySQLPath --version 2>&1
    Write-Host "✓ MySQL found: $mysqlVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: MySQL not found in PATH" -ForegroundColor Red
    Write-Host "  Please install MySQL or add it to your PATH" -ForegroundColor Red
    Write-Host "  Or specify MySQL path: -MySQLPath 'C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe'" -ForegroundColor Yellow
    exit 1
}

# Get MySQL root password if not provided
if ([string]::IsNullOrEmpty($RootPassword)) {
    $securePassword = Read-Host "Enter MySQL root password" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $RootPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

# Function to execute SQL script
function Execute-SQLScript {
    param(
        [string]$ScriptFile,
        [string]$User,
        [string]$Password,
        [string]$Database = ""
    )
    
    if (-not (Test-Path $ScriptFile)) {
        Write-Host "✗ ERROR: Script file not found: $ScriptFile" -ForegroundColor Red
        return $false
    }
    
    Write-Host "Executing: $ScriptFile..." -ForegroundColor Yellow
    
    try {
        if ([string]::IsNullOrEmpty($Database)) {
            $sqlContent = Get-Content $ScriptFile -Raw -Encoding UTF8
            $sqlContent | & $MySQLPath -u $User -p"$Password" 2>&1 | Out-Null
        } else {
            $sqlContent = Get-Content $ScriptFile -Raw -Encoding UTF8
            $sqlContent | & $MySQLPath -u $User -p"$Password" $Database 2>&1 | Out-Null
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Success: $ScriptFile" -ForegroundColor Green
            return $true
        } else {
            Write-Host "✗ ERROR: Failed to execute $ScriptFile" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "✗ ERROR: Exception executing $ScriptFile" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $false
    }
}

# Step 1: Create Database and User
Write-Host ""
Write-Host "Step 1: Creating database and user..." -ForegroundColor Cyan
if (Execute-SQLScript -ScriptFile "01-create-database-and-user.sql" -User "root" -Password $RootPassword) {
    Write-Host "✓ Database and user created successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to create database and user" -ForegroundColor Red
    exit 1
}

# Step 2: Create Tables
Write-Host ""
Write-Host "Step 2: Creating tables..." -ForegroundColor Cyan
if (Execute-SQLScript -ScriptFile "02-create-tables.sql" -User $DatabaseUser -Password $DatabasePassword -Database $DatabaseName) {
    Write-Host "✓ Tables created successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to create tables" -ForegroundColor Red
    exit 1
}

# Step 3: Load Test Data
Write-Host ""
Write-Host "Step 3: Loading test data..." -ForegroundColor Cyan
if (Execute-SQLScript -ScriptFile "03-load-test-data.sql" -User $DatabaseUser -Password $DatabasePassword -Database $DatabaseName) {
    Write-Host "✓ Test data loaded successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to load test data" -ForegroundColor Red
    exit 1
}

# Step 4: Verify Setup
Write-Host ""
Write-Host "Step 4: Verifying setup..." -ForegroundColor Cyan
if (Execute-SQLScript -ScriptFile "04-verify-setup.sql" -User $DatabaseUser -Password $DatabasePassword -Database $DatabaseName) {
    Write-Host "✓ Setup verification completed" -ForegroundColor Green
} else {
    Write-Host "⚠ Warning: Verification script had issues (check output above)" -ForegroundColor Yellow
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Database Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Database Configuration:" -ForegroundColor Yellow
Write-Host "  Database: $DatabaseName" -ForegroundColor White
Write-Host "  User: $DatabaseUser" -ForegroundColor White
Write-Host "  Password: $DatabasePassword" -ForegroundColor White
Write-Host ""
Write-Host "Test User Credentials:" -ForegroundColor Yellow
Write-Host "  Username: admin       Password: password123" -ForegroundColor White
Write-Host "  Username: manager1   Password: password123" -ForegroundColor White
Write-Host "  Username: user1      Password: password123" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Verify database connection in application" -ForegroundColor White
Write-Host "  2. Start Tomcat server" -ForegroundColor White
Write-Host "  3. Access application at http://localhost:8081/uob-t7-portal-mm-tomcat" -ForegroundColor White
Write-Host ""

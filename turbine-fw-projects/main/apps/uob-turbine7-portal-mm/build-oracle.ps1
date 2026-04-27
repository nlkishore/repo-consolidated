# ========================================================================
# UOB Turbine7 Portal MM - Build Script for Oracle Database
# ========================================================================
# This script builds the application WAR with Oracle database profile
# ========================================================================
# Usage:
#   .\build-oracle.ps1 [profile]
#   Profiles:
#     - tomcat-oracle    : Tomcat + Oracle (default)
#     - jboss8-oracle    : JBoss 8 + Oracle
# ========================================================================

param(
    [string]$Profile = "tomcat-oracle"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "UOB Turbine7 Portal MM - Oracle Build" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get project root directory
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

# Validate profile
$validProfiles = @("tomcat-oracle", "jboss8-oracle")
if ($Profile -notin $validProfiles) {
    Write-Host "✗ Invalid profile: $Profile" -ForegroundColor Red
    Write-Host "  Valid profiles: $($validProfiles -join ', ')" -ForegroundColor Yellow
    exit 1
}

Write-Host "Profile: $Profile" -ForegroundColor Yellow
Write-Host ""

# Copy Oracle Torque properties to replace MySQL properties
Write-Host "Step 1: Configuring Oracle database properties..." -ForegroundColor Yellow
$torqueOracle = Join-Path $projectRoot "webapp\src\main\webapp\WEB-INF\conf\Torque-oracle.properties"
$torqueTarget = Join-Path $projectRoot "webapp\src\main\webapp\WEB-INF\conf\Torque.properties"

if (Test-Path $torqueOracle) {
    Copy-Item $torqueOracle $torqueTarget -Force
    Write-Host "✓ Oracle Torque.properties configured" -ForegroundColor Green
} else {
    Write-Host "✗ Torque-oracle.properties not found: $torqueOracle" -ForegroundColor Red
    exit 1
}

# Build with Maven
Write-Host ""
Write-Host "Step 2: Building application with Maven..." -ForegroundColor Yellow
Write-Host "  Command: mvn clean package -P $Profile -DskipTests" -ForegroundColor Gray
Write-Host ""

try {
    mvn clean package -P $Profile -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Maven build failed" -ForegroundColor Red
        exit 1
    }
    Write-Host ""
    Write-Host "✓ Build successful!" -ForegroundColor Green
} catch {
    Write-Host "✗ Build error: $_" -ForegroundColor Red
    exit 1
}

# Find WAR file
$warFile = Get-ChildItem -Path "$projectRoot\webapp\target" -Filter "*.war" | Select-Object -First 1
if ($warFile) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Build Complete!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "WAR File: $($warFile.FullName)" -ForegroundColor Yellow
    Write-Host "Size: $([math]::Round($warFile.Length / 1MB, 2)) MB" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    if ($Profile -like "*jboss*") {
        Write-Host "  1. Deploy to JBoss 8:" -ForegroundColor Gray
        Write-Host "     Copy WAR to JBOSS_HOME/standalone/deployments/" -ForegroundColor Gray
        Write-Host "  2. Configure Oracle data source in JBoss" -ForegroundColor Gray
    } else {
        Write-Host "  1. Deploy to Tomcat:" -ForegroundColor Gray
        Write-Host "     Copy WAR to TOMCAT_HOME/webapps/" -ForegroundColor Gray
        Write-Host "  2. Ensure Oracle JDBC driver is in Tomcat lib/" -ForegroundColor Gray
    }
    Write-Host "  3. Start application server" -ForegroundColor Gray
    Write-Host "  4. Access application: http://localhost:8080/uob-*/app" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host "✗ WAR file not found in target directory" -ForegroundColor Red
    exit 1
}

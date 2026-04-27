# ========================================================================
# UOB Turbine7 Portal MM - JBoss 8 Deployment Script
# ========================================================================
# This script deploys the application WAR to JBoss 8 (WildFly 32+)
# ========================================================================
# Usage:
#   .\deploy-jboss8.ps1 [jbossHome] [warFile]
# ========================================================================

param(
    [string]$JbossHome = "",
    [string]$WarFile = ""
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "UOB Turbine7 Portal MM - JBoss 8 Deploy" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get project root directory
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# Find WAR file if not provided
if ([string]::IsNullOrEmpty($WarFile)) {
    $warFiles = @(
        "$projectRoot\webapp\target\uob-portal-mm-jboss-oracle.war",
        "$projectRoot\webapp\target\uob-portal-mm-jboss-mysql.war",
        "$projectRoot\webapp\target\uob-portal-mm-jboss.war"
    )
    
    foreach ($wf in $warFiles) {
        if (Test-Path $wf) {
            $WarFile = $wf
            break
        }
    }
    
    if ([string]::IsNullOrEmpty($WarFile)) {
        Write-Host "✗ WAR file not found. Please build first:" -ForegroundColor Red
        Write-Host "  .\build-oracle.ps1 -Profile jboss8-oracle" -ForegroundColor Yellow
        exit 1
    }
}

# Find JBoss home if not provided
if ([string]::IsNullOrEmpty($JbossHome)) {
    $possiblePaths = @(
        "$env:JBOSS_HOME",
        "C:\jboss",
        "C:\wildfly",
        "C:\applicationservers\jboss",
        "C:\applicationservers\wildfly"
    )
    
    foreach ($path in $possiblePaths) {
        if (-not [string]::IsNullOrEmpty($path) -and (Test-Path $path)) {
            $deploymentsDir = Join-Path $path "standalone\deployments"
            if (Test-Path $deploymentsDir) {
                $JbossHome = $path
                break
            }
        }
    }
    
    if ([string]::IsNullOrEmpty($JbossHome)) {
        Write-Host "✗ JBoss home not found. Please specify:" -ForegroundColor Red
        Write-Host "  .\deploy-jboss8.ps1 -JbossHome <path>" -ForegroundColor Yellow
        exit 1
    }
}

$deploymentsDir = Join-Path $JbossHome "standalone\deployments"
if (-not (Test-Path $deploymentsDir)) {
    Write-Host "✗ JBoss deployments directory not found: $deploymentsDir" -ForegroundColor Red
    exit 1
}

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  JBoss Home: $JbossHome" -ForegroundColor Gray
Write-Host "  Deployments: $deploymentsDir" -ForegroundColor Gray
Write-Host "  WAR File: $WarFile" -ForegroundColor Gray
Write-Host ""

# Copy WAR to deployments
$warFileName = Split-Path $WarFile -Leaf
$targetWar = Join-Path $deploymentsDir $warFileName

Write-Host "Deploying WAR..." -ForegroundColor Yellow
try {
    Copy-Item $WarFile $targetWar -Force
    Write-Host "✓ WAR copied to deployments directory" -ForegroundColor Green
    
    # Create .dodeploy marker file for auto-deployment
    $markerFile = "$targetWar.dodeploy"
    if (-not (Test-Path $markerFile)) {
        New-Item -ItemType File -Path $markerFile -Force | Out-Null
        Write-Host "✓ Deployment marker created" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Deployment Complete!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "  1. Configure Oracle data source in JBoss:" -ForegroundColor Gray
    Write-Host "     - Edit standalone.xml or use CLI" -ForegroundColor Gray
    Write-Host "     - JNDI name: java:jboss/datasources/kishore" -ForegroundColor Gray
    Write-Host "  2. Ensure Oracle JDBC driver is installed in JBoss" -ForegroundColor Gray
    Write-Host "  3. Start JBoss server (if not running)" -ForegroundColor Gray
    Write-Host "  4. Check deployment status in JBoss console" -ForegroundColor Gray
    Write-Host "  5. Access application: http://localhost:8080/uob-portal-mm-jboss-oracle/app" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Deployment error: $_" -ForegroundColor Red
    exit 1
}

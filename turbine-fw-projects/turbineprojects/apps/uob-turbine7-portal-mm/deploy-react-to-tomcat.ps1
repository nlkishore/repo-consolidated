# PowerShell script to build and deploy React application to Tomcat
# Usage: .\deploy-react-to-tomcat.ps1

param(
    [string]$TomcatPath = "C:\applicationservers\apache-tomcat-10.1.44\webapps",
    [string]$ContextPath = "react-admin-dashboard"
)

Write-Host "========================================" -ForegroundColor Green
Write-Host "React Application Deployment Script" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Step 1: Build React application
Write-Host "Step 1: Building React application..." -ForegroundColor Yellow
Set-Location react-admin-dashboard

if (-not (Test-Path "node_modules")) {
    Write-Host "Installing dependencies..." -ForegroundColor Cyan
    npm install
}

Write-Host "Building for production..." -ForegroundColor Cyan
npm run build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Exiting." -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..
Write-Host "✅ Build completed successfully" -ForegroundColor Green
Write-Host ""

# Step 2: Create WAR structure
Write-Host "Step 2: Creating WAR structure..." -ForegroundColor Yellow
$warDir = "react-admin-dashboard-war"
if (Test-Path $warDir) {
    Write-Host "Removing existing WAR directory..." -ForegroundColor Cyan
    Remove-Item $warDir -Recurse -Force
}

New-Item -ItemType Directory -Path $warDir -Force | Out-Null
New-Item -ItemType Directory -Path "$warDir\WEB-INF" -Force | Out-Null

# Copy React build files
Write-Host "Copying React build files..." -ForegroundColor Cyan
Copy-Item -Path "react-admin-dashboard\dist\*" -Destination $warDir -Recurse -Force

# Step 3: Create web.xml
Write-Host "Creating web.xml..." -ForegroundColor Cyan
$webXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>React Admin Dashboard</display-name>

    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>

    <!-- Error page for React Router (SPA routing) -->
    <error-page>
        <error-code>404</error-code>
        <location>/index.html</location>
    </error-page>

    <session-config>
        <session-timeout>60</session-timeout>
    </session-config>

</web-app>
"@
Set-Content -Path "$warDir\WEB-INF\web.xml" -Value $webXml

Write-Host "✅ WAR structure created" -ForegroundColor Green
Write-Host ""

# Step 4: Create WAR file
Write-Host "Step 3: Creating WAR file..." -ForegroundColor Yellow

# Check if jar command is available
$jarPath = Get-Command jar -ErrorAction SilentlyContinue
if (-not $jarPath) {
    Write-Host "ERROR: 'jar' command not found!" -ForegroundColor Red
    Write-Host "Please ensure JDK is installed and in PATH" -ForegroundColor Red
    Write-Host ""
    Write-Host "Alternative: Manually zip the WAR directory:" -ForegroundColor Yellow
    Write-Host "  1. Navigate to: $warDir" -ForegroundColor Gray
    Write-Host "  2. Select all files and folders" -ForegroundColor Gray
    Write-Host "  3. Right-click -> Send to -> Compressed (zipped) folder" -ForegroundColor Gray
    Write-Host "  4. Rename .zip to .war" -ForegroundColor Gray
    Set-Location ..
    exit 1
}

Set-Location $warDir
$warFile = "..\$ContextPath.war"

# Remove existing WAR if it exists
if (Test-Path $warFile) {
    Remove-Item $warFile -Force
}

Write-Host "Packaging WAR file..." -ForegroundColor Cyan
jar -cvf $warFile *

if ($LASTEXITCODE -ne 0) {
    Write-Host "WAR creation failed! Exiting." -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..
Write-Host "✅ WAR file created: $warFile" -ForegroundColor Green
Write-Host ""

# Step 5: Deploy to Tomcat
Write-Host "Step 4: Deploying to Tomcat..." -ForegroundColor Yellow

if (-not (Test-Path $TomcatPath)) {
    Write-Host "ERROR: Tomcat webapps directory not found: $TomcatPath" -ForegroundColor Red
    Write-Host "Please update TomcatPath parameter" -ForegroundColor Yellow
    exit 1
}

# Remove old deployment if exists
$oldDeployment = Join-Path $TomcatPath $ContextPath
if (Test-Path $oldDeployment) {
    Write-Host "Removing old deployment..." -ForegroundColor Cyan
    Remove-Item $oldDeployment -Recurse -Force
}

# Copy WAR file
$targetWar = Join-Path $TomcatPath "$ContextPath.war"
Write-Host "Copying WAR to Tomcat..." -ForegroundColor Cyan
Copy-Item $warFile $targetWar -Force

Write-Host "✅ Deployment complete!" -ForegroundColor Green
Write-Host ""

# Step 6: Summary
Write-Host "========================================" -ForegroundColor Green
Write-Host "Deployment Summary" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "WAR File:" -ForegroundColor Cyan
Write-Host "  Location: $targetWar" -ForegroundColor Gray
Write-Host ""
Write-Host "Application URL:" -ForegroundColor Cyan
Write-Host "  http://localhost:8081/$ContextPath/" -ForegroundColor White
Write-Host ""
Write-Host "Backend API:" -ForegroundColor Cyan
Write-Host "  http://localhost:8081/uob-t7-portal-mm-tomcat/api/*" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Wait for Tomcat to auto-deploy (or restart Tomcat)" -ForegroundColor Gray
Write-Host "  2. Access application in browser" -ForegroundColor Gray
Write-Host "  3. Test login: admin / password123" -ForegroundColor Gray
Write-Host "  4. Check browser console for any errors" -ForegroundColor Gray
Write-Host ""

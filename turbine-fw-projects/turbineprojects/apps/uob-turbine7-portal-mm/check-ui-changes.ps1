# UI Changes Diagnostic Script
# Run this script to check if UI changes are properly deployed

Write-Host "========================================" -ForegroundColor Green
Write-Host "UI Changes Diagnostic" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Check source files
Write-Host "1. Checking Source Files..." -ForegroundColor Yellow
$cssSource = "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\webapp\templates\app\admin\styles\admin-dashboard.css"
if (Test-Path $cssSource) {
    $sourceTime = (Get-Item $cssSource).LastWriteTime
    Write-Host "   CSS Source: $sourceTime" -ForegroundColor Cyan
} else {
    Write-Host "   CSS Source: NOT FOUND" -ForegroundColor Red
}

# Check built WAR
Write-Host ""
Write-Host "2. Checking Built WAR..." -ForegroundColor Yellow
$warFile = "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\target\uob-t7-portal-mm-tomcat.war"
if (Test-Path $warFile) {
    $warTime = (Get-Item $warFile).LastWriteTime
    Write-Host "   WAR File: $warTime" -ForegroundColor Cyan
    if ($sourceTime -and $warTime -lt $sourceTime) {
        Write-Host "   WARNING: WAR is older than source!" -ForegroundColor Red
        Write-Host "   ACTION: Run 'mvn clean package'" -ForegroundColor Yellow
    }
} else {
    Write-Host "   WAR File: NOT FOUND - Run 'mvn clean package'" -ForegroundColor Red
}

# Check deployed WAR
Write-Host ""
Write-Host "3. Checking Deployed WAR..." -ForegroundColor Yellow
$deployedWar = "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war"
if (Test-Path $deployedWar) {
    $deployedTime = (Get-Item $deployedWar).LastWriteTime
    Write-Host "   Deployed WAR: $deployedTime" -ForegroundColor Cyan
    if ($warTime -and $deployedTime -lt $warTime) {
        Write-Host "   WARNING: Deployed WAR is older than built WAR!" -ForegroundColor Red
        Write-Host "   ACTION: Redeploy WAR file" -ForegroundColor Yellow
    }
} else {
    Write-Host "   Deployed WAR: NOT FOUND" -ForegroundColor Red
}

# Check extracted files - CORRECT PATHS
Write-Host ""
Write-Host "4. Checking Extracted Files..." -ForegroundColor Yellow

# CSS file location (in templates/app/admin/styles/)
$extractedCss = "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css"
if (Test-Path $extractedCss) {
    $extractedTime = (Get-Item $extractedCss).LastWriteTime
    Write-Host "   Extracted CSS: $extractedTime" -ForegroundColor Cyan
    Write-Host "   Location: templates/app/admin/styles/admin-dashboard.css" -ForegroundColor Gray
    
    # Check sidebar width
    $content = Get-Content $extractedCss -Raw
    if ($content -match "width:\s*180px") {
        Write-Host "   Sidebar width: 180px (CORRECT)" -ForegroundColor Green
    } elseif ($content -match "width:\s*250px") {
        Write-Host "   Sidebar width: 250px (OLD VALUE - NEEDS UPDATE)" -ForegroundColor Red
    } else {
        Write-Host "   Sidebar width: CHECK MANUALLY" -ForegroundColor Yellow
    }
} else {
    Write-Host "   Extracted CSS: NOT FOUND" -ForegroundColor Red
    Write-Host "   Expected: templates/app/admin/styles/admin-dashboard.css" -ForegroundColor Yellow
    Write-Host "   ACTION: Restart Tomcat to extract WAR" -ForegroundColor Yellow
}

# JS file location
$extractedJs = "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\scripts\admin-dashboard.js"
if (Test-Path $extractedJs) {
    $jsTime = (Get-Item $extractedJs).LastWriteTime
    Write-Host "   Extracted JS: $jsTime" -ForegroundColor Cyan
    Write-Host "   Location: templates/app/admin/scripts/admin-dashboard.js" -ForegroundColor Gray
} else {
    Write-Host "   Extracted JS: NOT FOUND" -ForegroundColor Red
    Write-Host "   Expected: templates/app/admin/scripts/admin-dashboard.js" -ForegroundColor Yellow
}

# Check template file - CORRECT PATH
Write-Host ""
Write-Host "5. Checking Template Files..." -ForegroundColor Yellow
$templateFile = "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\screens\admin\Dashboard.vm"
if (Test-Path $templateFile) {
    $templateTime = (Get-Item $templateFile).LastWriteTime
    Write-Host "   Dashboard Template: $templateTime" -ForegroundColor Cyan
    Write-Host "   Location: templates/app/screens/admin/Dashboard.vm" -ForegroundColor Gray
} else {
    Write-Host "   Dashboard Template: NOT FOUND" -ForegroundColor Red
    Write-Host "   Expected: templates/app/screens/admin/Dashboard.vm" -ForegroundColor Yellow
    Write-Host "   ACTION: Restart Tomcat to extract WAR" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Diagnostic Complete" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. If WAR is old: mvn clean package" -ForegroundColor Gray
Write-Host "  2. If deployed WAR is old: Copy WAR to webapps folder" -ForegroundColor Gray
Write-Host "  3. If extracted files old: Restart Tomcat" -ForegroundColor Gray
Write-Host "  4. Clear browser cache: Ctrl+Shift+Delete" -ForegroundColor Gray
Write-Host "  5. Hard refresh: Ctrl+F5" -ForegroundColor Gray
Write-Host ""

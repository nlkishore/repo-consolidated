# Verify WAR file contains expected changes

Write-Host "=== WAR FILE VERIFICATION ===" -ForegroundColor Cyan
Write-Host ""

# Check source WAR
$sourceWar = "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\target\uob-t7-portal-mm-tomcat.war"
$deployedWar = "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war"

Write-Host "1. Source WAR File:" -ForegroundColor Yellow
if (Test-Path $sourceWar) {
    $info = Get-Item $sourceWar
    Write-Host "   Location: $sourceWar" -ForegroundColor Green
    Write-Host "   Size: $([math]::Round($info.Length/1MB, 2)) MB" -ForegroundColor White
    Write-Host "   Last Modified: $($info.LastWriteTime)" -ForegroundColor White
} else {
    Write-Host "   NOT FOUND!" -ForegroundColor Red
}

Write-Host ""
Write-Host "2. Deployed WAR File:" -ForegroundColor Yellow
if (Test-Path $deployedWar) {
    $info = Get-Item $deployedWar
    Write-Host "   Location: $deployedWar" -ForegroundColor Green
    Write-Host "   Size: $([math]::Round($info.Length/1MB, 2)) MB" -ForegroundColor White
    Write-Host "   Last Modified: $($info.LastWriteTime)" -ForegroundColor White
} else {
    Write-Host "   NOT FOUND!" -ForegroundColor Red
}

Write-Host ""
Write-Host "3. Compiled Class File:" -ForegroundColor Yellow
$classFile = "C:\Turbineprojects\uob-turbine7-portal-mm\turbine-model-controller\target\classes\flux\modules\screens\FluxScreen.class"
if (Test-Path $classFile) {
    $info = Get-Item $classFile
    Write-Host "   Location: $classFile" -ForegroundColor Green
    Write-Host "   Last Modified: $($info.LastWriteTime)" -ForegroundColor White
    $age = (Get-Date) - $info.LastWriteTime
    Write-Host "   Age: $([math]::Round($age.TotalMinutes, 1)) minutes" -ForegroundColor White
} else {
    Write-Host "   NOT FOUND!" -ForegroundColor Red
}

Write-Host ""
Write-Host "4. Source Code Verification:" -ForegroundColor Yellow
$sourceFile = "C:\Turbineprojects\uob-turbine7-portal-mm\turbine-model-controller\src\main\java\flux\modules\screens\FluxScreen.java"
if (Test-Path $sourceFile) {
    $hasGetSecurityService = Select-String -Path $sourceFile -Pattern "getSecurityService\(\)" -Quiet
    $hasGetUserManager = Select-String -Path $sourceFile -Pattern "security\.getUserManager\(\)\.getACL" -Quiet
    $hasUserObject = Select-String -Path $sourceFile -Pattern "User object:" -Quiet
    
    Write-Host "   getSecurityService() method: $(if ($hasGetSecurityService) { 'FOUND' } else { 'MISSING' })" -ForegroundColor $(if ($hasGetSecurityService) { "Green" } else { "Red" })
    Write-Host "   security.getUserManager().getACL: $(if ($hasGetUserManager) { 'FOUND' } else { 'MISSING' })" -ForegroundColor $(if ($hasGetUserManager) { "Green" } else { "Red" })
    Write-Host "   'User object:' log: $(if ($hasUserObject) { 'FOUND' } else { 'MISSING' })" -ForegroundColor $(if ($hasUserObject) { "Green" } else { "Red" })
} else {
    Write-Host "   Source file NOT FOUND!" -ForegroundColor Red
}

Write-Host ""
Write-Host "5. JAR File Check:" -ForegroundColor Yellow
$jarFile = "C:\Turbineprojects\uob-turbine7-portal-mm\turbine-model-controller\target\turbine-model-controller-1.0-SNAPSHOT.jar"
if (Test-Path $jarFile) {
    Write-Host "   JAR found, checking contents..." -ForegroundColor White
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $jar = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $jarFile))
    $fluxScreen = $jar.Entries | Where-Object { $_.FullName -eq "flux/modules/screens/FluxScreen.class" }
    if ($fluxScreen) {
        Write-Host "   FluxScreen.class found in JAR!" -ForegroundColor Green
        Write-Host "   Last Modified in JAR: $($fluxScreen.LastWriteTime)" -ForegroundColor White
    } else {
        Write-Host "   FluxScreen.class NOT in JAR!" -ForegroundColor Red
    }
    $jar.Dispose()
} else {
    Write-Host "   JAR file NOT FOUND!" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== SUMMARY ===" -ForegroundColor Cyan
Write-Host "If all checks show FOUND/GREEN, the changes are in the code." -ForegroundColor White
Write-Host "If Tomcat still uses old code, DELETE the extracted directory:" -ForegroundColor Yellow
Write-Host "  C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat" -ForegroundColor White

# Script to force Tomcat to reload the WAR by deleting the extracted directory
# This ensures Tomcat will extract the new WAR on next access

$tomcatDir = "C:\applicationservers\apache-tomcat-10.1.44"
$extractedWarDir = "$tomcatDir\webapps\uob-t7-portal-mm-tomcat"
$warFile = "$tomcatDir\webapps\uob-t7-portal-mm-tomcat.war"

Write-Host "=== Force Tomcat WAR Reload ===" -ForegroundColor Cyan
Write-Host ""

if (Test-Path $extractedWarDir) {
    Write-Host "Found extracted WAR directory: $extractedWarDir" -ForegroundColor Yellow
    Write-Host "This directory contains cached classes that Tomcat is using." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To force reload, you can:" -ForegroundColor Green
    Write-Host "  1. Stop Tomcat" -ForegroundColor White
    Write-Host "  2. Delete the extracted directory" -ForegroundColor White
    Write-Host "  3. Start Tomcat" -ForegroundColor White
    Write-Host ""
    
    $response = Read-Host "Do you want to delete the extracted directory now? (y/n)"
    if ($response -eq "y" -or $response -eq "Y") {
        Write-Host "Deleting $extractedWarDir ..." -ForegroundColor Yellow
        Remove-Item -Path $extractedWarDir -Recurse -Force
        Write-Host "✓ Deleted successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Next steps:" -ForegroundColor Cyan
        Write-Host "  1. Restart Tomcat (or it will auto-extract on next request)" -ForegroundColor White
        Write-Host "  2. Test the 'Add New User' link again" -ForegroundColor White
        Write-Host "  3. Check logs for new debug messages" -ForegroundColor White
    } else {
        Write-Host "Skipped. Please restart Tomcat manually to reload the WAR." -ForegroundColor Yellow
    }
} else {
    Write-Host "Extracted WAR directory does not exist." -ForegroundColor Green
    Write-Host "Tomcat will extract the WAR on next access." -ForegroundColor Green
}

Write-Host ""
Write-Host "WAR file timestamp: " -NoNewline -ForegroundColor Cyan
if (Test-Path $warFile) {
    $warInfo = Get-Item $warFile
    Write-Host $warInfo.LastWriteTime -ForegroundColor White
} else {
    Write-Host "Not found!" -ForegroundColor Red
}

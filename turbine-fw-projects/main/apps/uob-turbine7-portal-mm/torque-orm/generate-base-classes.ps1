# PowerShell script to generate Torque base classes
# This script temporarily moves modifiable classes, generates base classes, then moves them back

Write-Host "Generating Torque base classes..." -ForegroundColor Cyan

$backupDir = "src\main\java-backup"
$omDir = "src\main\java\com\uob\om"
$generatedDir = "target\generated-sources\com\uob\om"

# Step 1: Backup modifiable classes
Write-Host "Step 1: Backing up modifiable classes..." -ForegroundColor Yellow
if (Test-Path $omDir) {
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
    Get-ChildItem -Path $omDir -Filter "*.java" | Copy-Item -Destination $backupDir -Force
}

# Step 2: Remove modifiable classes temporarily
Write-Host "Step 2: Temporarily removing modifiable classes..." -ForegroundColor Yellow
Get-ChildItem -Path $omDir -Filter "Gtp*.java" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem -Path $omDir -Filter "Turbine*.java" -ErrorAction SilentlyContinue | Remove-Item -Force

# Step 3: Generate base classes
Write-Host "Step 3: Generating base classes..." -ForegroundColor Yellow
mvn clean generate-sources -DskipTests

# Step 4: Restore modifiable classes
Write-Host "Step 4: Restoring modifiable classes..." -ForegroundColor Yellow
if (Test-Path $backupDir) {
    Get-ChildItem -Path $backupDir -Filter "*.java" | Copy-Item -Destination $omDir -Force
    Remove-Item -Path $backupDir -Recurse -Force
}

# Step 5: Verify base classes were generated
Write-Host "Step 5: Verifying base classes..." -ForegroundColor Yellow
$baseUserFile = Join-Path $generatedDir "BaseGtpUser.java"
if (Test-Path $baseUserFile) {
    Write-Host "Base classes generated successfully!" -ForegroundColor Green
    Write-Host "Location: $generatedDir" -ForegroundColor Green
    $baseCount = (Get-ChildItem -Path $generatedDir -Filter "Base*.java" -ErrorAction SilentlyContinue).Count
    Write-Host "Found $baseCount base class files" -ForegroundColor Green
} else {
    Write-Host "Base classes not found. Check Torque generation output." -ForegroundColor Red
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan

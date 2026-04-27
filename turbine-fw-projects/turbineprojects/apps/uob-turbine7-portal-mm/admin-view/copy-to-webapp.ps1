# Copy Admin View Components to Webapp
# This script copies components from admin-view module to webapp templates

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$source = Join-Path $projectRoot "admin-view\src\main\webapp"
$target = Join-Path $projectRoot "webapp\src\main\webapp\templates\app\admin"

Write-Host "Copying admin view components..." -ForegroundColor Yellow
Write-Host "Source: $source" -ForegroundColor Gray
Write-Host "Target: $target" -ForegroundColor Gray
Write-Host ""

# Create target directories
if (-not (Test-Path $target)) {
    New-Item -ItemType Directory -Path $target -Force | Out-Null
}
if (-not (Test-Path "$target\components")) {
    New-Item -ItemType Directory -Path "$target\components" -Force | Out-Null
}
if (-not (Test-Path "$target\styles")) {
    New-Item -ItemType Directory -Path "$target\styles" -Force | Out-Null
}
if (-not (Test-Path "$target\scripts")) {
    New-Item -ItemType Directory -Path "$target\scripts" -Force | Out-Null
}

# Copy files
if (Test-Path "$source\components") {
    Copy-Item "$source\components\*" "$target\components\" -Recurse -Force
    Write-Host "✓ Copied components" -ForegroundColor Green
}

if (Test-Path "$source\styles") {
    Copy-Item "$source\styles\*" "$target\styles\" -Recurse -Force
    Write-Host "✓ Copied styles" -ForegroundColor Green
}

if (Test-Path "$source\scripts") {
    Copy-Item "$source\scripts\*" "$target\scripts\" -Recurse -Force
    Write-Host "✓ Copied scripts" -ForegroundColor Green
}

if (Test-Path "$source\Dashboard.vm") {
    Copy-Item "$source\Dashboard.vm" "$target\Dashboard.vm" -Force
    Write-Host "✓ Copied Dashboard.vm" -ForegroundColor Green
}

Write-Host ""
Write-Host "Copy complete!" -ForegroundColor Green
Write-Host "You can now build the project: mvn clean package" -ForegroundColor Yellow

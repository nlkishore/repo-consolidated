# One-time (or repeat-safe) MySQL setup for testbed: service, schema, user, tables.
# Usage:  .\scripts\setup-mysql.ps1

param(
    [string]$AdminUser = "kishore",
    [string]$AdminPassword = "Kish1381@"
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\env-testbed.ps1"

Start-MySql81

if (-not (Test-Path $MySqlExe)) {
    throw "mysql.exe not found at $MySqlExe"
}

$sqlDir = Join-Path $TestbedRoot "sql"
$bootstrap = Join-Path $sqlDir "00-create-testbed-db-user.sql"
$schema = Join-Path $sqlDir "01-create-testbed-schema.sql"

Write-Host "Creating testbed database and user (no password)..."
& $MySqlExe -u $AdminUser "-p$AdminPassword" -e "source $bootstrap"

Write-Host "Creating GTP tables..."
& $MySqlExe -u testbed testbed -e "source $schema"

Write-Host "Verifying tables..."
& $MySqlExe -u testbed testbed -e "SHOW TABLES LIKE 'GTP_%';"

Write-Host ""
Write-Host "MySQL testbed setup complete."
Write-Host "Next: pip install -e .[dev]  then  python -m testbed run-all --config config/settings.local.yaml"

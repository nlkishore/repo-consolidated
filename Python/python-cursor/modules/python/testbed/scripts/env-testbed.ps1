# Testbed MySQL 8.1 environment — source before running testbed commands.
# Usage:  . .\scripts\env-testbed.ps1

$ErrorActionPreference = "Stop"

$script:TestbedRoot = Split-Path -Parent $PSScriptRoot
$script:MySqlHome = "C:\Program Files\MySQL\MySQL Server 8.1"
$script:MySqlBin = Join-Path $MySqlHome "bin"
$script:MySqlExe = Join-Path $MySqlBin "mysql.exe"
$script:WorkbenchExe = "C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe"

# DB connection for testbed tool
$env:DB_HOST = "localhost"
$env:DB_NAME = "testbed"
$env:DB_USER = "testbed"
$env:DB_PASSWORD = ""

# Add MySQL CLI to PATH for this session
if (Test-Path $MySqlBin) {
    $env:PATH = "$MySqlBin;$env:PATH"
}

function Start-MySql81 {
    $svc = Get-Service -Name "MySQL81" -ErrorAction SilentlyContinue
    if (-not $svc) {
        Write-Warning "MySQL81 service not found."
        return
    }
    if ($svc.Status -ne "Running") {
        Write-Host "Starting MySQL81 service..."
        Start-Service MySQL81
    }
    Write-Host "MySQL81 status: $((Get-Service MySQL81).Status)"
}

function Start-MySqlWorkbench {
    if (Test-Path $WorkbenchExe) {
        Start-Process $WorkbenchExe
        Write-Host "Launched MySQL Workbench."
    } else {
        Write-Warning "MySQL Workbench not found at: $WorkbenchExe"
    }
}

Write-Host "Testbed env loaded."
Write-Host "  DB: $env:DB_USER@$env:DB_HOST/$env:DB_NAME (no password)"
Write-Host "  Root: $TestbedRoot"

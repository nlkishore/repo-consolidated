# Full testbed run: ensure MySQL up, seed all scenarios, validate, report.
# Usage:  .\scripts\run-testbed.ps1

param(
    [ValidateSet("run-all", "seed", "validate", "report", "reset")]
    [string]$Command = "run-all",
    [string]$Scenario = "",
    [switch]$NoReset
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\env-testbed.ps1"
Start-MySql81

Set-Location $TestbedRoot
$config = "config/settings.local.yaml"

$args = @("-m", "testbed", $Command, "--config", $config)
if ($Scenario) {
    $args += @("--scenario", $Scenario)
}
if ($Command -eq "run-all" -and $NoReset) {
    $args += "--no-reset"
}
if ($Command -eq "seed" -and -not $Scenario) {
    $args += "--all"
}

Write-Host "Running: python $($args -join ' ')"
python @args

if ($Command -eq "run-all" -or $Command -eq "report") {
    $html = Join-Path $TestbedRoot "testbed-reports\testbed-summary.html"
    if (Test-Path $html) {
        Write-Host "Report: $html"
    }
}

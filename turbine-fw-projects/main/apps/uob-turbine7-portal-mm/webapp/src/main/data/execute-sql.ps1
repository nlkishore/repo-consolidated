# ========================================================================
# Execute SQL Script - PowerShell Version
# ========================================================================
# This script properly executes SQL files in PowerShell
# ========================================================================

$ErrorActionPreference = "Continue"

# Configuration
$dbUser = "kishore"
$dbPassword = "Kish1381@"
$dbName = "kishore"
$sqlFile = "assign-roles-to-all-users.sql"
$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Assigning Roles to All Users" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verify MySQL exists
if (-not (Test-Path $mysqlExe)) {
    Write-Host "ERROR: MySQL not found at: $mysqlExe" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please use MySQL Workbench instead:" -ForegroundColor Yellow
    Write-Host "1. Open: C:\Program Files\MySQL\MySQL Workbench 8.0 CE\MySQLWorkbench.exe" -ForegroundColor White
    Write-Host "2. Connect to database 'kishore'" -ForegroundColor White
    Write-Host "3. Open: $(Resolve-Path $sqlFile -ErrorAction SilentlyContinue)" -ForegroundColor White
    Write-Host "4. Execute the script" -ForegroundColor White
    exit 1
}

# Verify SQL file exists
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "MySQL: $mysqlExe" -ForegroundColor Green
Write-Host "SQL File: $sqlFile" -ForegroundColor Green
Write-Host ""

# Method 1: Try using Get-Content with proper encoding
Write-Host "Executing SQL script..." -ForegroundColor Yellow

try {
    # Read SQL file with UTF8 encoding
    $sqlContent = [System.IO.File]::ReadAllText((Resolve-Path $sqlFile).Path, [System.Text.Encoding]::UTF8)
    
    # Execute using ProcessStartInfo for better control
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $mysqlExe
    $psi.Arguments = "-u $dbUser -p$dbPassword $dbName"
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    
    # Start process
    $process.Start() | Out-Null
    
    # Write SQL content to stdin
    $process.StandardInput.Write($sqlContent)
    $process.StandardInput.Close()
    
    # Read output
    $output = $process.StandardOutput.ReadToEnd()
    $errorOutput = $process.StandardError.ReadToEnd()
    
    # Wait for completion
    $process.WaitForExit()
    $exitCode = $process.ExitCode
    
    # Filter warnings
    $cleanOutput = ($output + $errorOutput) -split "`n" | Where-Object {
        $_ -notmatch "Warning" -and 
        $_ -notmatch "Using a password" -and 
        $_.Trim() -ne ""
    }
    
    if ($exitCode -eq 0) {
        Write-Host "SUCCESS: SQL script executed successfully!" -ForegroundColor Green
        if ($cleanOutput.Count -gt 0) {
            Write-Host ""
            $cleanOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
        }
    } else {
        Write-Host "ERROR: SQL execution failed (Exit code: $exitCode)" -ForegroundColor Red
        if ($cleanOutput.Count -gt 0) {
            Write-Host ""
            $cleanOutput | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
        exit 1
    }
    
} catch {
    Write-Host "ERROR: Exception occurred" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Try using MySQL Workbench instead (see instructions above)" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Verifying results..." -ForegroundColor Cyan
Write-Host "===========================" -ForegroundColor Cyan

$verifyQuery = "SELECT u.LOGIN_NAME, g.GROUP_NAME, r.ROLE_NAME FROM GTP_USER u LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID ORDER BY u.LOGIN_NAME;"

$verifyResult = & $mysqlExe -u $dbUser -p"$dbPassword" $dbName -e $verifyQuery 2>&1 | Where-Object { 
    $_ -notmatch "Warning" -and 
    $_ -notmatch "Using a password"
}

$verifyResult | ForEach-Object { Write-Host $_ -ForegroundColor White }

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Restart Tomcat" -ForegroundColor White
Write-Host "2. Login to application" -ForegroundColor White
Write-Host "3. Check UserProfile page" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

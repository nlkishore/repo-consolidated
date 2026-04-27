# PowerShell script to restart Tomcat and clear caches
# This ensures classloader cache is cleared so method overrides take effect

$tomcatHome = "C:\applicationservers\apache-tomcat-10.1.44"
$tomcatBin = "$tomcatHome\bin"
$workDir = "$tomcatHome\work"
$webappDir = "$tomcatHome\webapps\uob-t7-portal-mm-tomcat"
$logFile = "$webappDir\logs\avalon.log"

Write-Host "=== Tomcat Restart Script ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stop Tomcat
Write-Host "[1/5] Stopping Tomcat..." -ForegroundColor Yellow
$env:CATALINA_HOME = $tomcatHome
$env:CATALINA_BASE = $tomcatHome
$stopScript = "$tomcatBin\shutdown.bat"
if (Test-Path $stopScript) {
    & $stopScript
    Write-Host "  Waiting for Tomcat to stop..." -ForegroundColor Gray
    Start-Sleep -Seconds 5
    
    # Check if Tomcat is still running
    $tomcatProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.Path -like "*tomcat*" }
    if ($tomcatProcess) {
        Write-Host "  Tomcat still running, forcing stop..." -ForegroundColor Yellow
        Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 3
    }
    Write-Host "  [OK] Tomcat stopped" -ForegroundColor Green
} else {
    Write-Host "  [WARNING] shutdown.bat not found, trying to kill Java process..." -ForegroundColor Yellow
    Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 3
}

# Step 2: Clear work directory
Write-Host ""
Write-Host "[2/5] Clearing work directory..." -ForegroundColor Yellow
if (Test-Path $workDir) {
    Remove-Item -Path "$workDir\*" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  [OK] Work directory cleared: $workDir" -ForegroundColor Green
} else {
    Write-Host "  [INFO] Work directory doesn't exist (will be created on startup)" -ForegroundColor Gray
}

# Step 3: Clear exploded webapp directory (optional but recommended)
Write-Host ""
Write-Host "[3/5] Clearing exploded webapp directory..." -ForegroundColor Yellow
if (Test-Path $webappDir) {
    Remove-Item -Path $webappDir -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  [OK] Exploded webapp directory cleared: $webappDir" -ForegroundColor Green
} else {
    Write-Host "  [INFO] Exploded webapp directory doesn't exist" -ForegroundColor Gray
}

# Step 4: Start Tomcat
Write-Host ""
Write-Host "[4/5] Starting Tomcat..." -ForegroundColor Yellow
$env:CATALINA_HOME = $tomcatHome
$env:CATALINA_BASE = $tomcatHome
$startScript = "$tomcatBin\startup.bat"
if (Test-Path $startScript) {
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = $startScript
    $processInfo.WorkingDirectory = $tomcatBin
    $processInfo.UseShellExecute = $true
    $processInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
    $process = [System.Diagnostics.Process]::Start($processInfo)
    Write-Host "  [OK] Tomcat startup command executed" -ForegroundColor Green
    Write-Host "  Waiting for Tomcat to start..." -ForegroundColor Gray
    Start-Sleep -Seconds 10
    
    # Wait for Tomcat to be ready
    $maxWait = 60
    $waited = 0
    while ($waited -lt $maxWait) {
        $tomcatProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.Path -like "*tomcat*" }
        if ($tomcatProcess) {
            Write-Host "  [OK] Tomcat process is running" -ForegroundColor Green
            break
        }
        Start-Sleep -Seconds 2
        $waited += 2
        Write-Host "  Waiting... ($waited/$maxWait seconds)" -ForegroundColor Gray
    }
} else {
    Write-Host "  [ERROR] startup.bat not found!" -ForegroundColor Red
    exit 1
}

# Step 5: Wait for deployment and check logs
Write-Host ""
Write-Host "[5/5] Waiting for deployment and checking logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Wait for log file to be created
$maxWait = 30
$waited = 0
while ($waited -lt $maxWait) {
    if (Test-Path $logFile) {
        Write-Host "  [OK] Log file found" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $waited += 2
}

# Check for debug message in logs
Write-Host ""
Write-Host "=== Checking for Override Debug Message ===" -ForegroundColor Cyan
if (Test-Path $logFile) {
    $logContent = Get-Content $logFile -Tail 50 -ErrorAction SilentlyContinue
    if ($logContent -match "GtpUser\.retrieveAttachedObjects\(\) OVERRIDE CALLED") {
        Write-Host "  [SUCCESS] Override method IS being called!" -ForegroundColor Green
        Write-Host "  The debug message was found in the logs." -ForegroundColor Green
    } else {
        Write-Host "  [WARNING] Override method debug message NOT found in logs yet" -ForegroundColor Yellow
        Write-Host "  This might mean:" -ForegroundColor Yellow
        Write-Host "    - Deployment is still in progress" -ForegroundColor Gray
        Write-Host "    - Page hasn't been accessed yet (override only called on page access)" -ForegroundColor Gray
        Write-Host "    - Or there's still a classloader issue" -ForegroundColor Gray
    }
} else {
    Write-Host "  [INFO] Log file not found yet, deployment may still be in progress" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Restart Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Access a page: http://localhost:8081/uob-t7-portal-mm-tomcat/" -ForegroundColor White
Write-Host "2. Check logs for: 'GtpUser.retrieveAttachedObjects() OVERRIDE CALLED'" -ForegroundColor White
Write-Host "3. If the message appears, the override is working!" -ForegroundColor White
Write-Host "4. If ClassCastException persists, check the logs for the error" -ForegroundColor White
Write-Host ""

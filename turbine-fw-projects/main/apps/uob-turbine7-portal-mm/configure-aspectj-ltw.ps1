# PowerShell script to configure AspectJ Load-Time Weaving for Tomcat
# This script finds the aspectjweaver JAR and configures Tomcat to use it

$tomcatHome = "C:\applicationservers\apache-tomcat-10.1.44"
$mavenRepo = "$env:USERPROFILE\.m2\repository"
$aspectjVersion = "1.9.20"
$aspectjJar = "aspectjweaver-$aspectjVersion.jar"
$aspectjPath = Join-Path $mavenRepo "org\aspectj\aspectjweaver\$aspectjVersion\$aspectjJar"

Write-Host "=== AspectJ Load-Time Weaving Configuration ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Find aspectjweaver JAR
Write-Host "[1/3] Locating AspectJ weaver JAR..." -ForegroundColor Yellow
if (Test-Path $aspectjPath) {
    Write-Host "  [OK] Found: $aspectjPath" -ForegroundColor Green
    $aspectjJarPath = $aspectjPath
} else {
    Write-Host "  [WARNING] AspectJ weaver JAR not found in Maven repository" -ForegroundColor Yellow
    Write-Host "  Expected location: $aspectjPath" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Please run 'mvn dependency:copy' or build the project first" -ForegroundColor Yellow
    Write-Host "  to download the AspectJ weaver JAR." -ForegroundColor Yellow
    exit 1
}

# Step 2: Copy to Tomcat lib directory
Write-Host ""
Write-Host "[2/3] Copying AspectJ weaver to Tomcat lib..." -ForegroundColor Yellow
$tomcatLib = Join-Path $tomcatHome "lib"
if (-not (Test-Path $tomcatLib)) {
    New-Item -ItemType Directory -Path $tomcatLib -Force | Out-Null
}
$destPath = Join-Path $tomcatLib $aspectjJar
Copy-Item -Path $aspectjJarPath -Destination $destPath -Force
Write-Host "  [OK] Copied to: $destPath" -ForegroundColor Green

# Step 3: Configure setenv.bat
Write-Host ""
Write-Host "[3/3] Configuring Tomcat setenv.bat..." -ForegroundColor Yellow
$setenvBat = Join-Path $tomcatHome "bin\setenv.bat"
$javaAgentArg = "-javaagent:`"$destPath`""

if (Test-Path $setenvBat) {
    $content = Get-Content $setenvBat -Raw
    if ($content -match "-javaagent.*aspectjweaver") {
        Write-Host "  [INFO] AspectJ weaver already configured in setenv.bat" -ForegroundColor Gray
    } else {
        # Add JAVA_OPTS if it doesn't exist
        if ($content -notmatch "JAVA_OPTS") {
            Add-Content -Path $setenvBat -Value "`nset JAVA_OPTS=%JAVA_OPTS% $javaAgentArg"
        } else {
            # Append to existing JAVA_OPTS
            $content = $content -replace "(set JAVA_OPTS=.*)", "`$1 $javaAgentArg"
            Set-Content -Path $setenvBat -Value $content
        }
        Write-Host "  [OK] Added AspectJ weaver to setenv.bat" -ForegroundColor Green
    }
} else {
    # Create setenv.bat
    $setenvContent = @"
@echo off
REM AspectJ Load-Time Weaving Configuration
set JAVA_OPTS=%JAVA_OPTS% $javaAgentArg
"@
    Set-Content -Path $setenvBat -Value $setenvContent
    Write-Host "  [OK] Created setenv.bat with AspectJ configuration" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== Configuration Complete ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Restart Tomcat to apply changes" -ForegroundColor White
Write-Host "  2. Check Tomcat logs for AspectJ LTW initialization messages" -ForegroundColor White
Write-Host "  3. Access application and check logs for interceptor messages" -ForegroundColor White
Write-Host ""
Write-Host "To verify AspectJ LTW is working, check for:" -ForegroundColor Yellow
Write-Host "  - '[AspectJ] Load-time weaving enabled' in Tomcat logs" -ForegroundColor White
Write-Host "  - 'MaptoModelInterceptor: Intercepting maptoModel() call' in application logs" -ForegroundColor White

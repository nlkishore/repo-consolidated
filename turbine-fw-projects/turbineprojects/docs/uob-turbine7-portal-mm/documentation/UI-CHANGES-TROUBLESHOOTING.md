# UI Changes Not Reflecting - Troubleshooting Guide

## Quick Checklist

1. ✅ **Rebuilt the project** (`mvn clean package`)
2. ✅ **Redeployed WAR file** to Tomcat/JBoss
3. ✅ **Cleared browser cache** (Ctrl+Shift+Delete or Ctrl+F5)
4. ✅ **Checked browser console** for errors
5. ✅ **Verified files in deployed WAR** are updated

---

## 1. Browser Caching Issues

### Hard Refresh (Most Common Fix)
- **Windows/Linux**: `Ctrl + F5` or `Ctrl + Shift + R`
- **Mac**: `Cmd + Shift + R`
- **Chrome DevTools**: Right-click refresh button → "Empty Cache and Hard Reload"

### Clear Browser Cache
1. **Chrome/Edge**:
   - Press `Ctrl + Shift + Delete`
   - Select "Cached images and files"
   - Time range: "All time"
   - Click "Clear data"

2. **Firefox**:
   - Press `Ctrl + Shift + Delete`
   - Select "Cache"
   - Click "Clear Now"

### Disable Cache (Developer Mode)
1. Open **Chrome DevTools** (`F12`)
2. Go to **Network** tab
3. Check **"Disable cache"** checkbox
4. Keep DevTools open while testing

---

## 2. Server-Side Caching

### Check if WAR File is Updated
```powershell
# Check WAR file modification time
Get-Item "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war" | Select-Object LastWriteTime

# Compare with source files
Get-Item "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\target\uob-t7-portal-mm-tomcat.war" | Select-Object LastWriteTime
```

### Verify Files in Deployed WAR
```powershell
# Extract and check CSS file
cd C:\applicationservers\apache-tomcat-10.1.44\webapps
Expand-Archive -Path "uob-t7-portal-mm-tomcat.war" -DestinationPath "temp-check" -Force
Get-Content "temp-check\admin\styles\admin-dashboard.css" | Select-Object -First 20
Remove-Item "temp-check" -Recurse -Force
```

### Tomcat Cache Clearing
1. **Stop Tomcat**
2. **Delete work directory**:
   ```powershell
   Remove-Item "C:\applicationservers\apache-tomcat-10.1.44\work\*" -Recurse -Force
   ```
3. **Delete extracted WAR** (if exists):
   ```powershell
   Remove-Item "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat" -Recurse -Force
   ```
4. **Restart Tomcat**

---

## 3. File Versioning (Cache Busting)

### Check if Version Parameter is Working
1. Open browser DevTools (`F12`)
2. Go to **Network** tab
3. Reload page (`F5`)
4. Find `admin-dashboard.css` in the list
5. Check the **URL** - it should have `?v=` parameter
6. Check **Response Headers** - should not have `304 Not Modified`

### Verify CSS/JS Files are Loading
1. Open browser DevTools (`F12`)
2. Go to **Network** tab
3. Filter by **CSS** or **JS**
4. Reload page
5. Check if files load with **200 OK** status
6. If **304 Not Modified**, cache is still active

### Manual Cache Busting
If version parameter isn't working, manually update:
```velocity
## In Dashboard.vm, change version number
<link rel="stylesheet" href="$link.getContextPath()/admin/styles/admin-dashboard.css?v=2.0">
<script src="$link.getContextPath()/admin/scripts/admin-dashboard.js?v=2.0"></script>
```

---

## 4. Browser Developer Tools Inspection

### Check if CSS is Applied
1. Open DevTools (`F12`)
2. Go to **Elements** tab
3. Select the element (e.g., `.admin-sidebar`)
4. Check **Styles** panel:
   - Look for **strikethrough** styles (overridden)
   - Check **Computed** tab for actual values
   - Verify CSS file is loaded

### Check for CSS Errors
1. Open DevTools (`F12`)
2. Go to **Console** tab
3. Look for CSS-related errors:
   - `Failed to load resource`
   - `404 Not Found`
   - `SyntaxError`

### Verify File Paths
1. Open DevTools (`F12`)
2. Go to **Network** tab
3. Reload page
4. Check if CSS/JS files are found:
   - ✅ **200 OK** = File found
   - ❌ **404 Not Found** = Wrong path
   - ❌ **304 Not Modified** = Cached (need hard refresh)

---

## 5. File Location Verification

### Check if Files Exist in Correct Location
```powershell
# Check CSS file in deployed WAR (CORRECT PATH)
Test-Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css"

# Check JS file (CORRECT PATH)
Test-Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\scripts\admin-dashboard.js"

# Check template file (CORRECT PATH)
Test-Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\screens\admin\Dashboard.vm"
```

### Verify File Content
```powershell
# Check CSS file content (first few lines) - CORRECT PATH
Get-Content "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css" | Select-Object -First 10

# Check if sidebar width is updated
Select-String -Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css" -Pattern "width: 180px"
```

---

## 6. Maven Build Verification

### Verify Build Includes Changes
```powershell
cd C:\Turbineprojects\uob-turbine7-portal-mm

# Clean and rebuild
mvn clean package

# Check if build succeeded
if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
} else {
    Write-Host "Build failed! Check errors above." -ForegroundColor Red
}

# Verify WAR file was created
Test-Path "webapp\target\uob-t7-portal-mm-tomcat.war"
```

### Check Build Timestamp
```powershell
Get-Item "webapp\target\uob-t7-portal-mm-tomcat.war" | Select-Object LastWriteTime, Length
```

---

## 7. Deployment Verification

### Manual Deployment Check
```powershell
# Copy WAR file
Copy-Item "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\target\uob-t7-portal-mm-tomcat.war" `
    -Destination "C:\applicationservers\apache-tomcat-10.1.44\webapps\" -Force

# Verify copy succeeded
Get-Item "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war" | Select-Object LastWriteTime
```

### Check Tomcat Logs
```powershell
# Check for deployment errors
Get-Content "C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out" -Tail 50

# Check application logs
Get-Content "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\application.log" -Tail 20
```

---

## 8. Common Issues and Solutions

### Issue: CSS Changes Not Showing
**Solution**:
1. Hard refresh (`Ctrl + F5`)
2. Clear browser cache
3. Check if CSS file path is correct
4. Verify CSS syntax (no errors)
5. Check if CSS is being overridden by inline styles

### Issue: JavaScript Changes Not Working
**Solution**:
1. Hard refresh (`Ctrl + F5`)
2. Clear browser cache
3. Check browser console for JavaScript errors
4. Verify JavaScript file is loaded (Network tab)
5. Check if JavaScript is being cached

### Issue: Template Changes Not Showing
**Solution**:
1. Rebuild WAR (`mvn clean package`)
2. Redeploy WAR
3. Restart Tomcat
4. Clear Tomcat work directory
5. Check Velocity template syntax

### Issue: Layout Changes Not Reflecting
**Solution**:
1. Check if CSS classes are correct
2. Verify HTML structure matches CSS selectors
3. Check for CSS specificity issues (use DevTools)
4. Verify inline styles aren't overriding CSS
5. Check if W3.CSS is overriding custom styles

---

## 9. Quick Diagnostic Script

Save this as `check-ui-changes.ps1`:

```powershell
# UI Changes Diagnostic Script
Write-Host "========================================" -ForegroundColor Green
Write-Host "UI Changes Diagnostic" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Check source files
Write-Host "1. Checking Source Files..." -ForegroundColor Yellow
$cssSource = "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\webapp\templates\app\admin\styles\admin-dashboard.css"
if (Test-Path $cssSource) {
    $sourceTime = (Get-Item $cssSource).LastWriteTime
    Write-Host "   CSS Source: $sourceTime" -ForegroundColor Cyan
} else {
    Write-Host "   CSS Source: NOT FOUND" -ForegroundColor Red
}

# Check built WAR
Write-Host ""
Write-Host "2. Checking Built WAR..." -ForegroundColor Yellow
$warFile = "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\target\uob-t7-portal-mm-tomcat.war"
if (Test-Path $warFile) {
    $warTime = (Get-Item $warFile).LastWriteTime
    Write-Host "   WAR File: $warTime" -ForegroundColor Cyan
    if ($warTime -lt $sourceTime) {
        Write-Host "   WARNING: WAR is older than source!" -ForegroundColor Red
    }
} else {
    Write-Host "   WAR File: NOT FOUND - Run 'mvn clean package'" -ForegroundColor Red
}

# Check deployed WAR
Write-Host ""
Write-Host "3. Checking Deployed WAR..." -ForegroundColor Yellow
$deployedWar = "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war"
if (Test-Path $deployedWar) {
    $deployedTime = (Get-Item $deployedWar).LastWriteTime
    Write-Host "   Deployed WAR: $deployedTime" -ForegroundColor Cyan
    if ($deployedTime -lt $warTime) {
        Write-Host "   WARNING: Deployed WAR is older than built WAR!" -ForegroundColor Red
        Write-Host "   ACTION: Redeploy WAR file" -ForegroundColor Yellow
    }
} else {
    Write-Host "   Deployed WAR: NOT FOUND" -ForegroundColor Red
}

# Check extracted files
Write-Host ""
Write-Host "4. Checking Extracted Files..." -ForegroundColor Yellow
$extractedCss = "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\admin\styles\admin-dashboard.css"
if (Test-Path $extractedCss) {
    $extractedTime = (Get-Item $extractedCss).LastWriteTime
    Write-Host "   Extracted CSS: $extractedTime" -ForegroundColor Cyan
    
    # Check sidebar width
    $content = Get-Content $extractedCss -Raw
    if ($content -match "width:\s*180px") {
        Write-Host "   Sidebar width: 180px (CORRECT)" -ForegroundColor Green
    } else {
        Write-Host "   Sidebar width: NOT 180px (CHECK CSS)" -ForegroundColor Red
    }
} else {
    Write-Host "   Extracted CSS: NOT FOUND" -ForegroundColor Red
    Write-Host "   ACTION: Restart Tomcat to extract WAR" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Diagnostic Complete" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
```

---

## 10. Step-by-Step Troubleshooting Process

### Step 1: Verify Source Files Changed
```powershell
# Check file modification time
Get-Item "C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\webapp\templates\app\admin\styles\admin-dashboard.css" | Select-Object LastWriteTime
```

### Step 2: Rebuild Project
```powershell
cd C:\Turbineprojects\uob-turbine7-portal-mm
mvn clean package
```

### Step 3: Verify Build Success
- Check for `BUILD SUCCESS` message
- Verify WAR file exists in `webapp\target\`

### Step 4: Deploy WAR
```powershell
Copy-Item "webapp\target\uob-t7-portal-mm-tomcat.war" `
    -Destination "C:\applicationservers\apache-tomcat-10.1.44\webapps\" -Force
```

### Step 5: Restart Tomcat
- Stop Tomcat
- Clear work directory
- Start Tomcat

### Step 6: Clear Browser Cache
- `Ctrl + Shift + Delete`
- Select "Cached images and files"
- Clear data

### Step 7: Hard Refresh
- `Ctrl + F5` or `Ctrl + Shift + R`

### Step 8: Verify in Browser
- Open DevTools (`F12`)
- Check Network tab for CSS/JS files
- Verify files load with `200 OK`
- Check Elements tab for applied styles

---

## 11. Browser-Specific Cache Clearing

### Chrome
1. `Ctrl + Shift + Delete`
2. Select "Cached images and files"
3. Time range: "All time"
4. Click "Clear data"
5. Or: `Ctrl + F5` for hard refresh

### Firefox
1. `Ctrl + Shift + Delete`
2. Select "Cache"
3. Click "Clear Now"
4. Or: `Ctrl + F5` for hard refresh

### Edge
1. `Ctrl + Shift + Delete`
2. Select "Cached images and files"
3. Click "Clear now"
4. Or: `Ctrl + F5` for hard refresh

---

## 12. Advanced: Force File Reload

### Add Timestamp to URLs
```velocity
## In Dashboard.vm
<link rel="stylesheet" href="$link.getContextPath()/admin/styles/admin-dashboard.css?v=${DateTool.getSystemTime()}">
<script src="$link.getContextPath()/admin/scripts/admin-dashboard.js?v=${DateTool.getSystemTime()}"></script>
```

### Check if Version Parameter Works
1. Open page source (`Ctrl + U`)
2. Find CSS link
3. Copy URL
4. Paste in new tab
5. Check if file loads

---

## 13. Common Mistakes

### ❌ Wrong: Only changing source files
**✅ Correct**: Rebuild and redeploy

### ❌ Wrong: Not clearing browser cache
**✅ Correct**: Always hard refresh after deployment

### ❌ Wrong: Not restarting Tomcat
**✅ Correct**: Restart after deploying new WAR

### ❌ Wrong: Checking wrong file location
**✅ Correct**: Check deployed WAR, not source files

### ❌ Wrong: Ignoring browser console errors
**✅ Correct**: Always check DevTools console

---

## 14. Quick Fix Commands

### Complete Refresh (PowerShell)
```powershell
# Stop Tomcat
Stop-Service Tomcat10

# Clear work directory
Remove-Item "C:\applicationservers\apache-tomcat-10.1.44\work\*" -Recurse -Force

# Rebuild
cd C:\Turbineprojects\uob-turbine7-portal-mm
mvn clean package

# Deploy
Copy-Item "webapp\target\uob-t7-portal-mm-tomcat.war" `
    -Destination "C:\applicationservers\apache-tomcat-10.1.44\webapps\" -Force

# Start Tomcat
Start-Service Tomcat10

Write-Host "Deployment complete! Clear browser cache and hard refresh (Ctrl+F5)" -ForegroundColor Green
```

---

## 15. Verification Checklist

- [ ] Source files modified
- [ ] Project rebuilt (`mvn clean package`)
- [ ] WAR file deployed
- [ ] Tomcat restarted
- [ ] Work directory cleared
- [ ] Browser cache cleared
- [ ] Hard refresh performed (`Ctrl + F5`)
- [ ] DevTools checked for errors
- [ ] Network tab shows `200 OK` for CSS/JS
- [ ] Elements tab shows correct styles applied

---

## Still Not Working?

1. **Check browser console** for JavaScript errors
2. **Check Network tab** for failed file loads
3. **Verify file paths** are correct
4. **Check CSS specificity** - inline styles may override
5. **Try different browser** - rule out browser-specific issues
6. **Check Tomcat logs** for deployment errors
7. **Verify WAR extraction** - check if files exist in webapps folder

---

## Contact Points

- **Build Issues**: Check Maven output
- **Deployment Issues**: Check Tomcat logs
- **Runtime Issues**: Check application logs
- **Browser Issues**: Check DevTools console

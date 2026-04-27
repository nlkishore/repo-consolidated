# Tomcat Restart Instructions

**Date:** January 24, 2026  
**Purpose:** Clean deployment to ensure new classes are loaded

---

## What Was Done

✅ **Caches Cleared:**
- Work directory: `C:\applicationservers\apache-tomcat-10.1.44\work\Catalina\localhost\uob-t7-portal-mm-tomcat`
- Exploded webapp: `C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat`

✅ **WAR File Verified:**
- Location: `C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war`
- Size: 45.6 MB
- Modified: 01/24/2026 17:48:38

---

## Next Steps

### 1. Start Tomcat

**Option A: Using Command Prompt**
```cmd
cd C:\applicationservers\apache-tomcat-10.1.44\bin
startup.bat
```

**Option B: Using PowerShell**
```powershell
cd C:\applicationservers\apache-tomcat-10.1.44\bin
.\startup.bat
```

**Option C: Using the Restart Script**
```powershell
cd C:\Turbineprojects\uob-turbine7-portal-mm
.\restart-tomcat.ps1
```

### 2. Wait for Deployment

- Tomcat will automatically deploy the WAR file
- Wait approximately 30-60 seconds for deployment to complete
- Check `catalina.out` or the Tomcat console for deployment messages

### 3. Verify Deployment

**Check for class loading:**
```powershell
# Check application.log for class loading message
Get-Content C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\application.log | Select-String "GtpUser class loaded"
```

**Expected output:**
```
2026-01-24 XX:XX:XX,XXX [thread] INFO  com.uob.om.GtpUser - GtpUser class loaded - class: com.uob.om.GtpUser
```

### 4. Access the Application

**URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/`

### 5. Check for Override Messages

**After accessing a page, check logs for:**
```powershell
# Check application.log for override messages
Get-Content C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\application.log | Select-String "OVERRIDE CALLED"
```

**Expected messages:**
- `GtpUser.retrieveAttachedObjects() OVERRIDE CALLED`
- `GtpUser.getTurbineUserGroupRolesJoinTurbineGroup() OVERRIDE CALLED`

### 6. Check for Errors

**If ClassCastException persists:**
```powershell
# Check avalon.log for errors
Get-Content C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\avalon.log | Select-String "ClassCastException" -Context 5
```

---

## What to Look For

### ✅ Success Indicators

1. **Class Loading:**
   - `"GtpUser class loaded"` appears in `application.log`

2. **Override Working:**
   - `"GtpUser.retrieveAttachedObjects() OVERRIDE CALLED"` appears when accessing a page
   - `"GtpUser.getTurbineUserGroupRolesJoinTurbineGroup() OVERRIDE CALLED"` appears

3. **No Errors:**
   - No `ClassCastException` in logs
   - Page loads successfully

### ❌ Problem Indicators

1. **Override Not Working:**
   - No "OVERRIDE CALLED" messages in logs
   - `ClassCastException` still occurs
   - Stack trace shows `DefaultAbstractTurbineUser.retrieveAttachedObjects()` being called

2. **Deployment Issues:**
   - WAR file not found
   - Deployment errors in `catalina.out`
   - Application not accessible

---

## Troubleshooting

### If Override Still Not Working

1. **Verify WAR contains new classes:**
   ```powershell
   # Extract and check GtpUser.class timestamp
   Expand-Archive -Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war" -Destination "C:\temp\war-check" -Force
   Get-Item "C:\temp\war-check\WEB-INF\classes\com\uob\om\GtpUser.class" | Select-Object Name, LastWriteTime
   ```

2. **Check classloader:**
   - Verify only one version of `GtpUser.class` exists in the WAR
   - Check for duplicate JARs in `WEB-INF/lib`

3. **Full clean restart:**
   - Stop Tomcat completely
   - Delete work directory
   - Delete exploded webapp
   - Restart Tomcat

---

## Log File Locations

- **Application Log:** `C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\application.log`
- **Avalon Log:** `C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\avalon.log`
- **Catalina Log:** `C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out`
- **Torque Log:** `C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\torque.log`

---

## Summary

The caches have been cleared and the WAR file is ready. Start Tomcat and monitor the logs to verify:
1. Class is loaded correctly
2. Override methods are being invoked
3. ClassCastException is resolved

If the override still doesn't work after a clean restart, the framework is likely using reflection or a different mechanism that bypasses normal method dispatch.

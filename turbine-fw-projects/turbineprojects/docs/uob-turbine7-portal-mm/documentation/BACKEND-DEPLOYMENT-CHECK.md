# Backend Deployment Check Guide

## Issue: 404 Error on `/api/auth/check`

The React app is getting a 404 error when trying to access `/api/auth/check`, which means the backend API is not accessible.

## Quick Diagnosis

### 1. Check if Tomcat is Running

```bash
# Check if port 8080 is listening
netstat -an | findstr :8080

# Or try accessing Tomcat directly
curl http://localhost:8080
```

### 2. Check if WAR is Deployed

**Location**: `C:\applicationservers\apache-tomcat-10.1.44\webapps\`

**Expected files**:
- `uob-t7-portal-mm-tomcat.war` (WAR file)
- `uob-t7-portal-mm-tomcat/` (extracted directory)

### 3. Check Tomcat Logs

**Log file**: `C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out`

**Look for**:
- Spring DispatcherServlet initialization
- Any errors during startup
- Context path information

### 4. Test Backend Endpoint Directly

```bash
# Test auth check endpoint
curl http://localhost:8080/api/auth/check

# Test login endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

## Deployment Steps

### Step 1: Build the Project

```bash
cd C:\Turbineprojects\uob-turbine7-portal-mm
mvn clean package
```

**Expected output**: `webapp\target\uob-t7-portal-mm-tomcat.war`

### Step 2: Deploy to Tomcat

**Option A: Copy WAR file**
```powershell
Copy-Item `
  webapp\target\uob-t7-portal-mm-tomcat.war `
  C:\applicationservers\apache-tomcat-10.1.44\webapps\ `
  -Force
```

**Option B: Use Tomcat Manager** (if enabled)
- Access: http://localhost:8080/manager/html
- Upload WAR file through web interface

### Step 3: Verify Deployment

1. **Check Tomcat webapps directory**:
   ```powershell
   Get-ChildItem C:\applicationservers\apache-tomcat-10.1.44\webapps\
   ```

2. **Check if context is loaded**:
   - Look for `uob-t7-portal-mm-tomcat` directory
   - Check for `WEB-INF/web.xml` inside

3. **Check Tomcat logs**:
   ```powershell
   Get-Content C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out -Tail 50
   ```

### Step 4: Test API Endpoints

```bash
# Test auth check (should return JSON)
curl http://localhost:8080/api/auth/check

# Expected response:
# {"success":false,"message":"Not authenticated","user":null}
```

## Common Issues and Solutions

### Issue 1: WAR Not Deploying

**Symptoms**: No extracted directory in webapps

**Solutions**:
- Check Tomcat permissions
- Verify WAR file is not corrupted
- Check disk space
- Restart Tomcat

### Issue 2: Spring DispatcherServlet Not Initializing

**Symptoms**: 404 on all `/api/*` endpoints

**Check**:
- `web.xml` servlet mapping: `/api/*`
- SpringConfig class is in correct package
- No compilation errors in logs

**Solution**:
- Verify `SpringConfig.java` is in `com.uob.config` package
- Check `web.xml` servlet configuration
- Rebuild and redeploy

### Issue 3: Context Path Mismatch

**Symptoms**: Endpoints work but at wrong path

**Check**:
- Default context path is `/` (root)
- WAR filename determines context path
- `uob-t7-portal-mm-tomcat.war` → `/uob-t7-portal-mm-tomcat`

**Solution**:
- Rename WAR to `ROOT.war` for root context
- Or update React API base URL to match context path

### Issue 4: CORS Errors

**Symptoms**: Network request fails with CORS error

**Solution**:
- Verify `SpringConfig.java` CORS configuration
- Check allowed origins match React app URL
- Ensure `allowCredentials(true)` is set

## Verification Checklist

- [ ] Tomcat is running on port 8080
- [ ] WAR file exists in webapps directory
- [ ] WAR is extracted (directory exists)
- [ ] No errors in catalina.out log
- [ ] Spring DispatcherServlet initialized
- [ ] `/api/auth/check` endpoint returns JSON (not 404)
- [ ] CORS headers present in response
- [ ] React app proxy configured correctly

## Testing Commands

```powershell
# Check Tomcat status
Get-Process | Where-Object {$_.ProcessName -like "*tomcat*"}

# Check if port 8080 is in use
netstat -an | Select-String ":8080"

# Test backend endpoint
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/check" -UseBasicParsing

# View recent Tomcat logs
Get-Content C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out -Tail 100
```

## Next Steps

1. **If backend is not running**: Start Tomcat server
2. **If WAR is not deployed**: Copy WAR to webapps and restart Tomcat
3. **If endpoints return 404**: Check servlet mapping and Spring configuration
4. **If CORS errors**: Verify SpringConfig CORS settings
5. **If still having issues**: Check Tomcat logs for detailed error messages

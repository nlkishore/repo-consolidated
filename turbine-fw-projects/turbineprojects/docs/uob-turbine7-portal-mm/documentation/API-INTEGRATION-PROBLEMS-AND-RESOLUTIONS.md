# API Integration Problems and Resolutions

## Overview
This document details all the problems encountered while integrating the React SPA with the Spring REST API backend, along with their resolutions.

---

## Problem 1: Jakarta EE Migration - javax.servlet to jakarta.servlet

### Issue
**Error**: Compilation errors when building the project
```
error: package javax.servlet does not exist
```

**Root Cause**: 
- Tomcat 10.1.44 uses Jakarta EE 9+ which requires `jakarta.*` packages instead of `javax.*`
- The `AuthRestController.java` was using old `javax.servlet.*` imports

### Resolution Steps

1. **Updated Servlet Imports in AuthRestController.java**
   ```java
   // Before
   import javax.servlet.http.HttpServletRequest;
   import javax.servlet.http.HttpServletResponse;
   import javax.servlet.http.HttpSession;
   
   // After
   import jakarta.servlet.http.HttpServletRequest;
   import jakarta.servlet.http.HttpServletResponse;
   import jakarta.servlet.http.HttpSession;
   ```

2. **Updated SpringWebAppInitializer.java**
   ```java
   // Before
   import javax.servlet.ServletContext;
   import javax.servlet.ServletException;
   
   // After
   import jakarta.servlet.ServletContext;
   import jakarta.servlet.ServletException;
   ```

3. **Updated web.xml Namespace**
   ```xml
   <!-- Before -->
   <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
            version="3.1">
   
   <!-- After -->
   <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
            version="6.0">
   ```

4. **Updated DataSource References**
   ```xml
   <!-- Before -->
   <res-type>javax.sql.DataSource</res-type>
   
   <!-- After -->
   <res-type>jakarta.sql.DataSource</res-type>
   ```

**Files Modified**:
- `spring-rest-api/src/main/java/com/uob/controller/AuthRestController.java`
- `spring-rest-api/src/main/java/com/uob/config/SpringWebAppInitializer.java`
- `webapp/src/main/webapp/WEB-INF/web.xml`
- `webapp/src/main/webapp/WEB-INF/web-jboss.xml`

**Verification**:
```bash
mvn clean package
# Should compile without errors
```

---

## Problem 2: Duplicate Class Compilation Error

### Issue
**Error**:
```
[ERROR] error: duplicate class: com.uob.controller.AuthRestController
```

**Root Cause**: 
- `AuthRestController.java` was moved from `com/uob/` to `com/uob/controller/`
- The old file was not deleted, causing duplicate class definition

### Resolution Steps

1. **Identified Duplicate Files**
   ```bash
   # Found two files:
   - com/uob/AuthRestController.java (old location)
   - com/uob/controller/AuthRestController.java (new location)
   ```

2. **Deleted Old File**
   ```bash
   # Removed: spring-rest-api/src/main/java/com/uob/AuthRestController.java
   ```

3. **Verified Package Structure**
   - Ensured only one file exists: `com/uob/controller/AuthRestController.java`
   - Verified package declaration: `package com.uob.controller;`

**Files Modified**:
- Deleted: `spring-rest-api/src/main/java/com/uob/AuthRestController.java`

**Verification**:
```bash
mvn clean package
# Should compile successfully
```

---

## Problem 3: Network Error - 404 Not Found

### Issue
**Error in Browser Console**:
```
GET http://localhost:3000/api/auth/check 404 (Not Found)
Error checking authentication: SyntaxError: Unexpected token '<', "<!doctype "... is not valid JSON
```

**Root Cause**: 
- Backend server not running or not deployed
- React app trying to connect to non-existent endpoint
- Error response was HTML (404 page) instead of JSON

### Resolution Steps

1. **Improved Error Handling in App.tsx**
   ```typescript
   // Added proper HTTP status checking
   if (!response.ok) {
     if (response.status === 404) {
       console.warn('Auth endpoint not found. Backend may not be running or deployed.')
       setIsAuthenticated(false)
       return
     }
     // Handle other errors...
   }
   ```

2. **Verified Backend Deployment**
   ```bash
   # Check if WAR is deployed
   Get-ChildItem C:\applicationservers\apache-tomcat-10.1.44\webapps\
   
   # Test backend directly
   curl http://localhost:8081/api/auth/check
   ```

3. **Created Deployment Checklist**
   - Documented steps to verify backend deployment
   - Created troubleshooting guide for network errors

**Files Modified**:
- `react-admin-dashboard/src/App.tsx`
- Created: `BACKEND-DEPLOYMENT-CHECK.md`
- Created: `NETWORK-ERROR-TROUBLESHOOTING.md`

**Verification**:
```bash
# Test backend endpoint
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/check
# Expected: {"success":false,"message":"Not authenticated","user":null}
```

---

## Problem 4: Port Configuration Mismatch

### Issue
**Error**: React app configured for port 8080, but Tomcat running on 8081

**Root Cause**: 
- Tomcat server.xml configured for port 8081
- React Vite proxy still pointing to port 8080

### Resolution Steps

1. **Updated Vite Proxy Configuration**
   ```typescript
   // vite.config.ts
   // Before
   target: 'http://localhost:8080',
   
   // After
   target: 'http://localhost:8081',
   ```

2. **Updated Error Messages**
   ```typescript
   // Login.tsx
   // Before
   setError('Network error. Please check if backend is running on http://localhost:8080')
   
   // After
   setError('Network error. Please check if backend is running on http://localhost:8081')
   ```

**Files Modified**:
- `react-admin-dashboard/vite.config.ts`
- `react-admin-dashboard/src/components/Login.tsx`

**Verification**:
```bash
# Verify Tomcat port
Get-Content C:\applicationservers\apache-tomcat-10.1.44\conf\server.xml | Select-String "port="
# Should show: <Connector port="8081"
```

---

## Problem 5: Context Path Mismatch

### Issue
**Error**: 
```
GET http://localhost:8081/api/auth/check 404 (Not Found)
```

**But**:
```
GET http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/check 200 OK
```

**Root Cause**: 
- WAR file deployed with context path `/uob-t7-portal-mm-tomcat`
- React app making requests to `/api/*` without context path
- Backend expects `/uob-t7-portal-mm-tomcat/api/*`

### Resolution Steps

1. **Added Proxy Rewrite Rule in Vite Config**
   ```typescript
   // vite.config.ts
   proxy: {
     '/api': {
       target: 'http://localhost:8081',
       changeOrigin: true,
       secure: false,
       ws: true,
       rewrite: (path) => path.replace(/^\/api/, '/uob-t7-portal-mm-tomcat/api'),
       // ... rest of config
     }
   }
   ```

2. **How It Works**:
   - React app calls: `/api/auth/check`
   - Vite proxy intercepts the request
   - Rewrites to: `/uob-t7-portal-mm-tomcat/api/auth/check`
   - Forwards to: `http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/check`
   - Backend receives correct path and responds

**Files Modified**:
- `react-admin-dashboard/vite.config.ts`

**Verification**:
```bash
# Test with context path
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/check
# Expected: {"success":false,"message":"Not authenticated","user":null}

# After restarting React dev server, test from browser
# Network tab should show successful API calls
```

---

## Problem 6: CORS Configuration

### Issue
**Error in Browser Console**:
```
Access to fetch at 'http://localhost:8081/api/auth/login' from origin 'http://localhost:3000' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present
```

**Root Cause**: 
- Spring backend not configured to allow requests from React app origin
- Missing CORS headers in response

### Resolution Steps

1. **Verified SpringConfig.java CORS Configuration**
   ```java
   @Override
   public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/api/**")
               .allowedOrigins("http://localhost:3000", "http://localhost:5173")
               .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
               .allowedHeaders("*")
               .allowCredentials(true)
               .maxAge(3600);
   }
   ```

2. **Ensured React App Uses Credentials**
   ```typescript
   // api.ts
   const api = axios.create({
     baseURL: API_BASE_URL,
     headers: {
       'Content-Type': 'application/json'
     },
     withCredentials: true // Important for session cookies
   })
   ```

**Files Verified**:
- `spring-rest-api/src/main/java/com/uob/config/SpringConfig.java`
- `react-admin-dashboard/src/services/api.ts`

**Verification**:
```bash
# Check CORS headers in response
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/login \
     -v
# Should see Access-Control-Allow-Origin header
```

---

## Problem 7: Session Management Issues

### Issue
**Error**: Login successful but subsequent API calls fail with 401 Unauthorized

**Root Cause**: 
- Session not being properly created or maintained
- Cookies not being sent with requests
- Session attributes not matching Turbine expectations

### Resolution Steps

1. **Updated AuthRestController to Set Session Properly**
   ```java
   // Create session and set user
   HttpSession session = request.getSession(true);
   session.setAttribute("user", user);
   session.setAttribute("username", username);
   
   // Create RunData-like session attributes for Turbine compatibility
   session.setAttribute("turbine.user", user);
   ```

2. **Ensured React App Sends Credentials**
   ```typescript
   // All API calls include credentials
   credentials: 'include' // In fetch calls
   withCredentials: true  // In axios config
   ```

3. **Updated CORS to Allow Credentials**
   ```java
   .allowCredentials(true) // In SpringConfig.java
   ```

**Files Modified**:
- `spring-rest-api/src/main/java/com/uob/controller/AuthRestController.java`
- `react-admin-dashboard/src/services/api.ts`
- `react-admin-dashboard/src/App.tsx`
- `react-admin-dashboard/src/components/Login.tsx`

**Verification**:
```bash
# Test login and check cookies
curl -X POST http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' \
  -c cookies.txt \
  -v

# Test subsequent call with cookies
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/check \
  -b cookies.txt
```

---

## Problem 8: Error Response Parsing

### Issue
**Error**: 
```
SyntaxError: Unexpected token '<', "<!doctype "... is not valid JSON
```

**Root Cause**: 
- Backend returning HTML error page (404/500) instead of JSON
- React app trying to parse HTML as JSON
- No error handling for non-JSON responses

### Resolution Steps

1. **Added Response Status Checking**
   ```typescript
   // App.tsx - checkAuth function
   if (!response.ok) {
     if (response.status === 404) {
       console.warn('Auth endpoint not found. Backend may not be running or deployed.')
       setIsAuthenticated(false)
       return
     }
     // Try to parse error message
     try {
       const errorData = await response.json()
       console.warn('Auth check failed:', errorData.message || response.statusText)
     } catch {
       console.warn('Auth check failed:', response.status, response.statusText)
     }
     setIsAuthenticated(false)
     return
   }
   ```

2. **Improved Login Error Handling**
   ```typescript
   // Login.tsx
   if (!response.ok) {
     try {
       const errorData = await response.json()
       setError(errorData.message || `Login failed: ${response.status} ${response.statusText}`)
     } catch {
       setError(`Login failed: ${response.status} ${response.statusText}`)
     }
     return
   }
   ```

**Files Modified**:
- `react-admin-dashboard/src/App.tsx`
- `react-admin-dashboard/src/components/Login.tsx`

---

## Summary of All Resolutions

### Configuration Changes

1. **Jakarta EE Migration**
   - ✅ Updated all `javax.*` to `jakarta.*` imports
   - ✅ Updated web.xml to Jakarta EE 6.0 namespace
   - ✅ Updated DataSource references

2. **Port Configuration**
   - ✅ Updated Vite proxy to port 8081
   - ✅ Updated error messages to reference correct port

3. **Context Path**
   - ✅ Added proxy rewrite rule for context path
   - ✅ `/api/*` → `/uob-t7-portal-mm-tomcat/api/*`

4. **CORS Configuration**
   - ✅ SpringConfig allows React app origins
   - ✅ Credentials enabled for session management

5. **Error Handling**
   - ✅ Proper HTTP status checking
   - ✅ JSON parsing error handling
   - ✅ User-friendly error messages

### Files Modified

**Backend**:
- `spring-rest-api/src/main/java/com/uob/controller/AuthRestController.java`
- `spring-rest-api/src/main/java/com/uob/config/SpringWebAppInitializer.java`
- `spring-rest-api/src/main/java/com/uob/config/SpringConfig.java`
- `webapp/src/main/webapp/WEB-INF/web.xml`
- `webapp/src/main/webapp/WEB-INF/web-jboss.xml`

**Frontend**:
- `react-admin-dashboard/vite.config.ts`
- `react-admin-dashboard/src/App.tsx`
- `react-admin-dashboard/src/components/Login.tsx`
- `react-admin-dashboard/src/services/api.ts`

**Documentation**:
- `JAKARTA-EE-MIGRATION-SUMMARY.md`
- `NETWORK-ERROR-TROUBLESHOOTING.md`
- `BACKEND-DEPLOYMENT-CHECK.md`
- `API-INTEGRATION-PROBLEMS-AND-RESOLUTIONS.md` (this file)

---

## Testing Checklist

After applying all fixes, verify:

- [ ] Backend compiles without errors: `mvn clean package`
- [ ] WAR deployed to Tomcat: Check `webapps` directory
- [ ] Tomcat running on port 8081: Check `server.xml`
- [ ] Backend endpoint accessible: `curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/check`
- [ ] React dev server running: `npm run dev` on port 3000
- [ ] Vite proxy configured: Check `vite.config.ts`
- [ ] Login works: Test with `admin` / `password123`
- [ ] Session maintained: Check cookies in browser DevTools
- [ ] API calls succeed: Check Network tab in browser
- [ ] No CORS errors: Check browser console
- [ ] Error handling works: Test with invalid credentials

---

## Quick Reference

### Backend URL Structure
```
Base URL: http://localhost:8081
Context Path: /uob-t7-portal-mm-tomcat
API Base: /uob-t7-portal-mm-tomcat/api
Auth Endpoints:
  - POST /uob-t7-portal-mm-tomcat/api/auth/login
  - GET  /uob-t7-portal-mm-tomcat/api/auth/check
  - POST /uob-t7-portal-mm-tomcat/api/auth/logout
```

### React App Configuration
```
Dev Server: http://localhost:3000
Proxy: /api/* → http://localhost:8081/uob-t7-portal-mm-tomcat/api/*
Credentials: Enabled (withCredentials: true)
```

### Common Commands
```bash
# Rebuild backend
mvn clean package

# Test backend endpoint
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/auth/check

# Start React dev server
cd react-admin-dashboard
npm run dev

# Check Tomcat logs
Get-Content C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.*.log -Tail 50
```

---

## Lessons Learned

1. **Jakarta EE Migration**: Always check container version and use appropriate package namespaces
2. **Context Paths**: WAR filenames determine context paths in Tomcat
3. **Proxy Configuration**: Vite proxy rewrite rules are essential for context path handling
4. **Error Handling**: Always check HTTP status before parsing JSON responses
5. **Session Management**: CORS credentials must be enabled on both client and server
6. **Port Configuration**: Keep all configuration files in sync when changing ports
7. **Deployment Verification**: Always test endpoints directly before troubleshooting client issues

---

## Additional Resources

- [Jakarta EE Migration Guide](https://jakarta.ee/specifications/platform/)
- [Vite Proxy Configuration](https://vitejs.dev/config/server-options.html#server-proxy)
- [Spring CORS Documentation](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
- [Tomcat Context Path Configuration](https://tomcat.apache.org/tomcat-10.1-doc/config/context.html)

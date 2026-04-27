# Network Error Troubleshooting Guide

## Issue: React Application Shows Network Error

When the React application tries to connect to the backend server, it shows a network error.

## Root Causes and Solutions

### 1. Backend Server Not Running

**Symptom**: Network error when React app tries to call `/api/*` endpoints.

**Solution**:
```bash
# Ensure Tomcat is running with the deployed WAR
# Check Tomcat logs: C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out
# Verify WAR is deployed: C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat.war
```

### 2. CORS Configuration Issues

**Symptom**: Browser console shows CORS errors.

**Check**: `SpringConfig.java` CORS configuration:
```java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000", "http://localhost:5173")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true)
    .maxAge(3600);
```

**Solution**: Ensure React app is running on port 3000 or 5173.

### 3. Vite Proxy Configuration

**Check**: `react-admin-dashboard/vite.config.ts`:
```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false,
      ws: true
    }
  }
}
```

**Solution**: 
- Ensure backend is running on `http://localhost:8080`
- Restart Vite dev server after changes: `npm run dev`

### 4. API Endpoint Mapping

**Check**: `web.xml` servlet mapping:
```xml
<servlet-mapping>
    <servlet-name>springDispatcher</servlet-name>
    <url-pattern>/api/*</url-pattern>
</servlet-mapping>
```

**Verify**: REST controllers are mapped correctly:
- `@RequestMapping("/auth")` → `/api/auth/*`
- `@RequestMapping("/users")` → `/api/users/*`
- `@RequestMapping("/groups")` → `/api/groups/*`
- `@RequestMapping("/roles")` → `/api/roles/*`
- `@RequestMapping("/permissions")` → `/api/permissions/*`

### 5. Session/Cookie Issues

**Symptom**: Login works but subsequent API calls fail.

**Check**: 
- React API service uses `withCredentials: true`
- Backend CORS allows credentials: `allowCredentials(true)`
- Session is properly created in `AuthRestController.login()`

### 6. Jakarta EE Migration Issues

**Fixed Issues**:
- ✅ `javax.servlet.*` → `jakarta.servlet.*` in `AuthRestController.java`
- ✅ `web.xml` namespace updated to Jakarta EE 6.0
- ✅ `javax.sql.DataSource` → `jakarta.sql.DataSource` in `web.xml`

**Verify**: All imports use `jakarta.*` instead of `javax.*`

## Testing Steps

### Step 1: Verify Backend is Running

```bash
# Check if Tomcat is running
curl http://localhost:8080/api/auth/check

# Expected: JSON response with authentication status
```

### Step 2: Test API Endpoints Directly

```bash
# Test login endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' \
  -c cookies.txt

# Test users endpoint (after login)
curl http://localhost:8080/api/users \
  -b cookies.txt
```

### Step 3: Verify React App Configuration

1. **Check Vite Dev Server**:
   ```bash
   cd react-admin-dashboard
   npm run dev
   # Should start on http://localhost:3000
   ```

2. **Check Browser Console**:
   - Open DevTools (F12)
   - Go to Network tab
   - Try to login
   - Check if requests are going to `/api/auth/login`
   - Verify response status codes

3. **Check Proxy Logs**:
   - Vite proxy errors appear in terminal
   - Check for "Proxy error" messages

### Step 4: Common Network Error Messages

| Error Message | Cause | Solution |
|--------------|-------|----------|
| `Network Error` | Backend not running | Start Tomcat server |
| `CORS policy blocked` | CORS misconfiguration | Check SpringConfig.java |
| `404 Not Found` | Wrong URL pattern | Verify servlet mapping |
| `500 Internal Server Error` | Backend exception | Check Tomcat logs |
| `Connection refused` | Wrong port/URL | Verify proxy target URL |

## Debugging Checklist

- [ ] Tomcat server is running on port 8080
- [ ] WAR file is deployed successfully
- [ ] Spring DispatcherServlet is initialized (check logs)
- [ ] CORS is configured correctly
- [ ] React app is running on port 3000
- [ ] Vite proxy is configured correctly
- [ ] Browser console shows no CORS errors
- [ ] Network tab shows API requests
- [ ] API responses have correct status codes
- [ ] Session cookies are being set/received

## Quick Fix Commands

```bash
# Rebuild and redeploy
cd C:\Turbineprojects\uob-turbine7-portal-mm
mvn clean package
# Copy WAR to Tomcat webapps
Copy-Item webapp\target\uob-t7-portal-mm-tomcat.war C:\applicationservers\apache-tomcat-10.1.44\webapps\

# Restart React dev server
cd react-admin-dashboard
npm run dev

# Check Tomcat logs
Get-Content C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out -Tail 50
```

## Additional Resources

- Spring CORS Documentation: https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html
- Vite Proxy Configuration: https://vitejs.dev/config/server-options.html#server-proxy
- Jakarta EE Migration Guide: https://jakarta.ee/specifications/platform/9/

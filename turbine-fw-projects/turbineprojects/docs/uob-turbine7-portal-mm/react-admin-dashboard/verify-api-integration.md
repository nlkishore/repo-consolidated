# API Integration Verification Guide

## Quick Verification Steps

### 1. Verify Backend API is Running

```bash
# Test Users API
curl http://localhost:8080/api/users

# Test Groups API
curl http://localhost:8080/api/groups

# Test Roles API
curl http://localhost:8080/api/roles

# Test Permissions API
curl http://localhost:8080/api/permissions
```

**Expected Response**: JSON array with data

### 2. Verify CORS Configuration

Check that `SpringConfig.java` includes:
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:5173")
            ...
}
```

### 3. Start React App

```bash
cd react-admin-dashboard
npm install
npm run dev
```

### 4. Verify API Calls in Browser

1. Open React app: `http://localhost:3000`
2. Open Browser DevTools (F12)
3. Go to **Network** tab
4. Filter by "Fetch/XHR"
5. Refresh page
6. Verify you see:
   - `GET /api/users` → Status 200
   - `GET /api/groups` → Status 200
   - `GET /api/roles` → Status 200
   - `GET /api/permissions` → Status 200

### 5. Check Console for Errors

- Open **Console** tab in DevTools
- Look for any API errors
- If API fails, you should see "Falling back to mock data" warning

### 6. Verify Data Display

- Check that User Details section shows real users from database
- Check that Groups, Roles, Permissions sections show real data
- Verify search functionality works

## Troubleshooting

### API Returns 404

**Problem**: API endpoints not found

**Solutions**:
1. Verify WAR is deployed to Tomcat
2. Check `web.xml` has `/api/*` servlet mapping
3. Verify Spring controllers are in `com.uob.controller` package
4. Check Tomcat logs for errors

### CORS Errors

**Problem**: Browser console shows CORS errors

**Solutions**:
1. Verify `SpringConfig.java` has CORS configuration
2. Check React app URL matches allowed origins
3. Ensure `withCredentials: true` in axios config
4. Try accessing API directly: `curl http://localhost:8080/api/users`

### Data Not Loading

**Problem**: React app shows "No data found" or mock data

**Solutions**:
1. Check Network tab - are API calls successful?
2. Verify API returns data (test with curl)
3. Check browser console for errors
4. Verify DTO field mapping is correct
5. Check that database has data

### Proxy Errors

**Problem**: Vite proxy not working

**Solutions**:
1. Verify Tomcat is running on port 8080
2. Check `vite.config.ts` proxy configuration
3. Try accessing API directly: `http://localhost:8080/api/users`
4. Restart Vite dev server

## Expected API Response Format

### Users Response
```json
[
  {
    "userId": 1,
    "loginName": "admin",
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@uob.com",
    "confirmed": true
  }
]
```

### Groups Response
```json
[
  {
    "groupId": 1,
    "groupName": "ADMINISTRATORS"
  }
]
```

### Roles Response
```json
[
  {
    "roleId": 1,
    "roleName": "turbineadmin"
  }
]
```

### Permissions Response
```json
[
  {
    "permissionId": 1,
    "permissionName": "ADMIN"
  }
]
```

## Success Indicators

✅ **API Integration is Working When**:
- Network tab shows successful API calls (200 status)
- Real data from database is displayed
- No CORS errors in console
- Search functionality works
- Data updates when database changes

## Next Steps After Verification

1. **Remove Mock Data**: Once API is working, remove mock data fallback
2. **Add Error Handling**: Enhance error messages for users
3. **Add Loading States**: Show loading indicators during API calls
4. **Add Pagination**: For large datasets
5. **Add Real-time Updates**: WebSocket or polling for live data

# React SPA API Integration Guide

## Overview

This guide explains how the React Admin Dashboard integrates with the REST API endpoints from the `uob-turbine7-portal-mm` project.

## API Endpoints

The Spring REST API provides the following endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users` | GET | Get all users |
| `/api/users/{loginName}` | GET | Get user by login name |
| `/api/groups` | GET | Get all groups |
| `/api/groups/{groupName}` | GET | Get group by name |
| `/api/roles` | GET | Get all roles |
| `/api/roles/{roleName}` | GET | Get role by name |
| `/api/roles/{roleName}/permissions` | GET | Get permissions for a role |
| `/api/permissions` | GET | Get all permissions |
| `/api/permissions/{permissionName}` | GET | Get permission by name |

## DTO Structure

### UserDTO
```java
{
  "userId": Integer,
  "loginName": String,
  "firstName": String,
  "lastName": String,
  "email": String,
  "confirmed": Boolean,
  "lastLogin": Date,
  "created": Date,
  "modified": Date
}
```

### GroupDTO
```java
{
  "groupId": Integer,
  "groupName": String
}
```

### RoleDTO
```java
{
  "roleId": Integer,
  "roleName": String
}
```

### PermissionDTO
```java
{
  "permissionId": Integer,
  "permissionName": String
}
```

## React Type Mapping

The React application maps DTO fields to React types:

| DTO Field | React Field |
|-----------|-------------|
| `userId` → `id` | `loginName` → `name` |
| `groupId` → `id` | `groupName` → `name` |
| `roleId` → `id` | `roleName` → `name` |
| `permissionId` → `id` | `permissionName` → `name` |

## CORS Configuration

CORS is configured in `SpringConfig.java` to allow requests from:
- `http://localhost:3000` (React dev server)
- `http://localhost:5173` (Vite default port)

## Vite Proxy Configuration

The Vite dev server proxies `/api/*` requests to `http://localhost:8080` (Tomcat server).

## Integration Steps

### 1. Start Backend Server

```bash
# Ensure Tomcat is running with the deployed WAR
# The REST API endpoints will be available at:
# http://localhost:8080/api/*
```

### 2. Start React Development Server

```bash
cd react-admin-dashboard
npm install
npm run dev
```

The React app will be available at `http://localhost:3000`

### 3. API Calls

The React app automatically calls the REST API endpoints:
- On page load: Fetches all users, groups, roles, and permissions
- On search: Filters data client-side (can be enhanced with server-side search)

## Error Handling

The React app includes fallback to mock data if API calls fail:
- Logs error to console
- Falls back to mock data for development
- Allows development even if backend is not running

## Testing API Integration

### Test API Endpoints Directly

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

### Test from React App

1. Open browser console (F12)
2. Navigate to React app: `http://localhost:3000`
3. Check Network tab for API calls
4. Verify data is loaded from `/api/users`, `/api/groups`, etc.

## Troubleshooting

### CORS Errors

If you see CORS errors:
1. Verify `SpringConfig.java` has CORS configuration
2. Check that React app URL matches allowed origins
3. Ensure `withCredentials: true` is set in axios config

### API Not Found (404)

1. Verify Spring REST API is deployed
2. Check `web.xml` has `/api/*` servlet mapping
3. Verify controllers are in `com.uob` package (scanned by SpringConfig)

### Data Not Loading

1. Check browser console for errors
2. Verify API endpoints return data (test with curl)
3. Check Network tab for failed requests
4. Verify DTO field mapping is correct

## Production Deployment

For production:
1. Build React app: `npm run build`
2. Copy `dist` folder to `webapp/src/main/webapp/react-dashboard/`
3. Update API base URL if needed (currently `/api`)
4. Ensure CORS allows production domain

## API Response Examples

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

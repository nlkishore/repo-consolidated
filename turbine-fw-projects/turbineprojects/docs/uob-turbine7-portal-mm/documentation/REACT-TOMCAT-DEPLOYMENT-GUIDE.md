# React Application Deployment to Tomcat Server

## Overview
This guide explains how to build and deploy the React SPA (Single Page Application) to Apache Tomcat server, integrating it with the existing Turbine/Spring backend.

---

## Prerequisites

- Node.js and npm installed
- Maven installed
- Apache Tomcat 10.1.44 running on port 8081
- Backend WAR already deployed (`uob-t7-portal-mm-tomcat.war`)

---

## Deployment Options

### Option 1: Deploy as Separate Web Application (Recommended)
Deploy React app as a separate webapp in Tomcat, accessible at a different context path.

### Option 2: Integrate into Existing WAR
Copy React build files into the existing WAR file, serving them from the same context path.

---

## Option 1: Separate Web Application Deployment

### Step 1: Update API Base URL for Production

The React app needs to know the backend context path in production (no Vite proxy).

**File**: `react-admin-dashboard/src/services/api.ts`

```typescript
// Update API_BASE_URL to include context path
const API_BASE_URL = '/uob-t7-portal-mm-tomcat/api'
```

Or use environment variables:

**File**: `react-admin-dashboard/src/services/api.ts`

```typescript
// Use environment variable with fallback
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/uob-t7-portal-mm-tomcat/api'
```

**File**: `react-admin-dashboard/.env.production`

```env
VITE_API_BASE_URL=/uob-t7-portal-mm-tomcat/api
```

### Step 2: Update Vite Configuration for Production Build

**File**: `react-admin-dashboard/vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        ws: true,
        rewrite: (path) => path.replace(/^\/api/, '/uob-t7-portal-mm-tomcat/api'),
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('Proxy error:', err)
          })
        }
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false, // Disable sourcemaps in production for smaller build
    // Configure base path if deploying to subdirectory
    // base: '/react-admin/'
  },
  // Configure base path for assets
  base: './' // Use relative paths for assets
})
```

### Step 3: Build the React Application

```bash
cd react-admin-dashboard

# Install dependencies (if not already done)
npm install

# Build for production
npm run build
```

This creates a `dist` folder with:
- `index.html`
- `assets/` folder with JS, CSS, and other assets
- Other static files

### Step 4: Create WAR File Structure

```bash
# Create temporary directory for WAR structure
mkdir react-admin-dashboard-war
cd react-admin-dashboard-war

# Create WEB-INF directory
mkdir WEB-INF

# Copy React build files
cp -r ../react-admin-dashboard/dist/* .

# Or on Windows PowerShell:
Copy-Item -Path ..\react-admin-dashboard\dist\* -Destination . -Recurse
```

### Step 5: Create web.xml for React App

**File**: `react-admin-dashboard-war/WEB-INF/web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>React Admin Dashboard</display-name>

    <!-- Welcome file -->
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>

    <!-- Error page for React Router (SPA routing) -->
    <error-page>
        <error-code>404</error-code>
        <location>/index.html</location>
    </error-page>

    <!-- Session configuration -->
    <session-config>
        <session-timeout>60</session-timeout>
    </session-config>

</web-app>
```

### Step 6: Package as WAR File

**On Windows (PowerShell)**:
```powershell
# Navigate to WAR directory
cd react-admin-dashboard-war

# Create WAR file (requires jar command from JDK)
jar -cvf ..\react-admin-dashboard.war *

# Or use Maven (if you have a Maven project structure)
```

**On Linux/Mac**:
```bash
cd react-admin-dashboard-war
jar -cvf ../react-admin-dashboard.war *
```

### Step 7: Deploy to Tomcat

```powershell
# Copy WAR to Tomcat webapps directory
Copy-Item react-admin-dashboard.war `
  C:\applicationservers\apache-tomcat-10.1.44\webapps\ `
  -Force
```

Tomcat will automatically:
1. Extract the WAR file
2. Create context path: `/react-admin-dashboard`
3. Start the application

### Step 8: Access the Application

- **React App**: `http://localhost:8081/react-admin-dashboard/`
- **Backend API**: `http://localhost:8081/uob-t7-portal-mm-tomcat/api/*`

---

## Option 2: Integrate into Existing WAR

### Step 1: Build React Application

```bash
cd react-admin-dashboard
npm run build
```

### Step 2: Copy Build Files to WAR Structure

**Before building the backend WAR**, copy React files:

```powershell
# Copy React build to webapp directory
Copy-Item -Path react-admin-dashboard\dist\* `
  -Destination webapp\src\main\webapp\react-admin\ `
  -Recurse -Force

# Create directory if it doesn't exist
New-Item -ItemType Directory -Path webapp\src\main\webapp\react-admin -Force
```

### Step 3: Update web.xml to Serve React App

**File**: `webapp/src/main/webapp/WEB-INF/web.xml`

Add welcome file and error page:

```xml
<welcome-file-list>
    <welcome-file>app</welcome-file>
    <welcome-file>react-admin/index.html</welcome-file>
</welcome-file-list>

<!-- Error page for React Router -->
<error-page>
    <error-code>404</error-code>
    <location>/react-admin/index.html</location>
</error-page>
```

### Step 4: Rebuild and Deploy WAR

```bash
# Rebuild the WAR with React files included
mvn clean package

# Deploy to Tomcat
Copy-Item webapp\target\uob-t7-portal-mm-tomcat.war `
  C:\applicationservers\apache-tomcat-10.1.44\webapps\ `
  -Force
```

### Step 5: Access the Application

- **React App**: `http://localhost:8081/uob-t7-portal-mm-tomcat/react-admin/`
- **Backend API**: `http://localhost:8081/uob-t7-portal-mm-tomcat/api/*`

---

## Configuration for Production

### Update API Base URL

**File**: `react-admin-dashboard/src/services/api.ts`

```typescript
// For Option 1 (separate webapp)
const API_BASE_URL = '/uob-t7-portal-mm-tomcat/api'

// For Option 2 (integrated)
const API_BASE_URL = '/uob-t7-portal-mm-tomcat/api' // Same context path
```

### Environment-Based Configuration

Create environment files:

**File**: `react-admin-dashboard/.env.development`
```env
VITE_API_BASE_URL=/api
```

**File**: `react-admin-dashboard/.env.production`
```env
VITE_API_BASE_URL=/uob-t7-portal-mm-tomcat/api
```

**File**: `react-admin-dashboard/src/services/api.ts`
```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
})
```

### Update Vite Config for Base Path

If deploying to a subdirectory:

**File**: `react-admin-dashboard/vite.config.ts`

```typescript
export default defineConfig({
  // ... other config
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    // If deploying to /react-admin/, set base:
    // base: '/react-admin/'
    base: './' // Use relative paths (recommended)
  }
})
```

---

## Handling React Router (If Used)

If your React app uses React Router, you need to configure Tomcat to serve `index.html` for all routes.

### Option A: Error Page Configuration (Recommended)

**File**: `WEB-INF/web.xml`

```xml
<!-- Redirect 404 to index.html for React Router -->
<error-page>
    <error-code>404</error-code>
    <location>/index.html</location>
</error-page>
```

### Option B: URL Rewrite Filter

Add Tuckey URL Rewrite Filter to handle SPA routing:

**Add to `pom.xml`**:
```xml
<dependency>
    <groupId>org.tuckey</groupId>
    <artifactId>urlrewritefilter</artifactId>
    <version>4.0.4</version>
</dependency>
```

**File**: `WEB-INF/urlrewrite.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<urlrewrite>
    <rule>
        <from>^/react-admin/.*$</from>
        <to>/react-admin/index.html</to>
    </rule>
</urlrewrite>
```

---

## Automated Deployment Script

**File**: `deploy-react-to-tomcat.ps1`

```powershell
# Build React application
Write-Host "Building React application..." -ForegroundColor Yellow
Set-Location react-admin-dashboard
npm run build
Set-Location ..

# Create WAR structure
Write-Host "Creating WAR structure..." -ForegroundColor Yellow
$warDir = "react-admin-dashboard-war"
if (Test-Path $warDir) {
    Remove-Item $warDir -Recurse -Force
}
New-Item -ItemType Directory -Path $warDir -Force | Out-Null
New-Item -ItemType Directory -Path "$warDir\WEB-INF" -Force | Out-Null

# Copy React build files
Copy-Item -Path "react-admin-dashboard\dist\*" -Destination $warDir -Recurse -Force

# Create web.xml
$webXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    <display-name>React Admin Dashboard</display-name>
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>
    <error-page>
        <error-code>404</error-code>
        <location>/index.html</location>
    </error-page>
    <session-config>
        <session-timeout>60</session-timeout>
    </session-config>
</web-app>
"@
Set-Content -Path "$warDir\WEB-INF\web.xml" -Value $webXml

# Create WAR file
Write-Host "Creating WAR file..." -ForegroundColor Yellow
Set-Location $warDir
jar -cvf ..\react-admin-dashboard.war *
Set-Location ..

# Deploy to Tomcat
Write-Host "Deploying to Tomcat..." -ForegroundColor Yellow
$tomcatWebapps = "C:\applicationservers\apache-tomcat-10.1.44\webapps"
Copy-Item react-admin-dashboard.war $tomcatWebapps -Force

Write-Host "Deployment complete!" -ForegroundColor Green
Write-Host "Access at: http://localhost:8081/react-admin-dashboard/" -ForegroundColor Cyan
```

**Usage**:
```powershell
.\deploy-react-to-tomcat.ps1
```

---

## Verification Steps

### 1. Check WAR Deployment

```powershell
# Verify WAR file exists
Test-Path C:\applicationservers\apache-tomcat-10.1.44\webapps\react-admin-dashboard.war

# Check if extracted
Test-Path C:\applicationservers\apache-tomcat-10.1.44\webapps\react-admin-dashboard
```

### 2. Check Tomcat Logs

```powershell
Get-Content C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.*.log -Tail 50 | Select-String "react-admin"
```

### 3. Test Application Access

```bash
# Test React app
curl http://localhost:8081/react-admin-dashboard/

# Test API connectivity (from browser console)
# Should be able to call: /uob-t7-portal-mm-tomcat/api/auth/check
```

### 4. Browser Testing

1. Open: `http://localhost:8081/react-admin-dashboard/`
2. Check browser console for errors
3. Check Network tab for API calls
4. Verify login works
5. Test all features

---

## Troubleshooting

### Issue: 404 on React Routes

**Solution**: Ensure `web.xml` has error page configuration:
```xml
<error-page>
    <error-code>404</error-code>
    <location>/index.html</location>
</error-page>
```

### Issue: API Calls Fail (CORS or 404)

**Solution**: 
1. Verify API base URL includes context path: `/uob-t7-portal-mm-tomcat/api`
2. Check CORS configuration in `SpringConfig.java`
3. Verify backend is running and accessible

### Issue: Assets Not Loading (404 on JS/CSS)

**Solution**: 
1. Check `vite.config.ts` base path configuration
2. Use relative paths: `base: './'`
3. Verify assets are in `dist/assets/` folder
4. Check browser Network tab for actual asset URLs

### Issue: Blank Page

**Solution**:
1. Check browser console for JavaScript errors
2. Verify `index.html` is being served
3. Check if React build completed successfully
4. Verify all dependencies are included in build

### Issue: Session Not Working

**Solution**:
1. Verify `withCredentials: true` in axios config
2. Check CORS allows credentials: `.allowCredentials(true)`
3. Verify cookies are being set (check browser DevTools)
4. Ensure same domain or proper CORS configuration

---

## Production Optimizations

### 1. Disable Source Maps

**File**: `vite.config.ts`
```typescript
build: {
  sourcemap: false // Disable in production
}
```

### 2. Enable Compression

Configure Tomcat to compress responses:

**File**: `conf/server.xml`
```xml
<Connector port="8081" ...>
    <Compression on="on"
                 compressionMinSize="2048"
                 noCompressionUserAgents="gozilla, traviata"
                 compressableMimeType="text/html,text/xml,text/javascript,text/css,application/javascript,application/json"/>
</Connector>
```

### 3. Cache Static Assets

Add cache headers in `web.xml` or use a reverse proxy (nginx/Apache HTTP Server).

### 4. Environment Variables

Use environment-specific configuration:
- `.env.development` - Development settings
- `.env.production` - Production settings
- `.env.local` - Local overrides (gitignored)

---

## Quick Reference

### Build Commands
```bash
# Development
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

### Deployment Locations

**Option 1 (Separate WAR)**:
- React App: `http://localhost:8081/react-admin-dashboard/`
- Backend API: `http://localhost:8081/uob-t7-portal-mm-tomcat/api/*`

**Option 2 (Integrated)**:
- React App: `http://localhost:8081/uob-t7-portal-mm-tomcat/react-admin/`
- Backend API: `http://localhost:8081/uob-t7-portal-mm-tomcat/api/*`

### File Structure

**Option 1 WAR Structure**:
```
react-admin-dashboard.war
├── index.html
├── assets/
│   ├── index-*.js
│   ├── index-*.css
│   └── ...
└── WEB-INF/
    └── web.xml
```

**Option 2 Integrated Structure**:
```
uob-t7-portal-mm-tomcat.war
├── app/ (Turbine)
├── react-admin/
│   ├── index.html
│   └── assets/
└── WEB-INF/
    └── web.xml
```

---

## Next Steps

1. **Choose deployment option** (Option 1 recommended for separation)
2. **Update API base URL** for production
3. **Build React application**
4. **Create and deploy WAR file**
5. **Test application** in browser
6. **Configure production optimizations**

---

## Additional Resources

- [Vite Build Documentation](https://vitejs.dev/guide/build.html)
- [Tomcat Deployment Guide](https://tomcat.apache.org/tomcat-10.1-doc/deployer-howto.html)
- [React Router Deployment](https://reactrouter.com/en/main/start/overview)
- [Apache Tomcat Configuration](https://tomcat.apache.org/tomcat-10.1-doc/config/)

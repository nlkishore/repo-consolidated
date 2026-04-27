# Admin Dashboard File Locations Reference

## Correct File Paths in Deployed Application

When Tomcat extracts the WAR file, files are located in the following structure:

### Base Directory
```
C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\
```

### CSS File Location
```
templates/app/admin/styles/admin-dashboard.css
```
**Full Path:**
```
C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css
```

### JavaScript File Location
```
templates/app/admin/scripts/admin-dashboard.js
```
**Full Path:**
```
C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\scripts\admin-dashboard.js
```

### Template File Location
```
templates/app/screens/admin/Dashboard.vm
```
**Full Path:**
```
C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\screens\admin\Dashboard.vm
```

### Component Templates Location
```
templates/app/screens/admin/components/
├── Header.vm
├── Sidebar.vm
├── SearchBox.vm
├── ResultsGrid.vm
├── TooltipWindow.vm
├── ContextualMenu.vm
└── WorkAreaSection.vm
```

---

## Source File Locations (Before Build)

### CSS Source
```
C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\webapp\templates\app\admin\styles\admin-dashboard.css
```

### JavaScript Source
```
C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\webapp\templates\app\admin\scripts\admin-dashboard.js
```

### Template Source
```
C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\webapp\templates\app\screens\admin\Dashboard.vm
```

---

## Important Notes

1. **Files are NOT in `/admin/` directory** - They are in `/templates/app/admin/`
2. **Templates are NOT in `/WEB-INF/classes/`** - They are in `/templates/app/screens/`
3. **Tomcat extracts WAR automatically** - Files appear in `webapps/uob-t7-portal-mm-tomcat/` after deployment
4. **URL Path vs File Path** - URL uses `/admin/styles/` but file is at `/templates/app/admin/styles/`

---

## How to Verify Files Are Deployed

### PowerShell Commands

```powershell
# Check CSS file
Test-Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css"

# Check JS file
Test-Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\scripts\admin-dashboard.js"

# Check template file
Test-Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\screens\admin\Dashboard.vm"

# Check file modification time
Get-Item "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css" | Select-Object LastWriteTime
```

### Check File Content

```powershell
# Verify sidebar width in CSS
Select-String -Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css" -Pattern "width:\s*180px"

# View first 20 lines of CSS
Get-Content "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin\styles\admin-dashboard.css" | Select-Object -First 20
```

---

## URL Mapping

When accessing files via browser:

| Browser URL | Actual File Path |
|------------|------------------|
| `/uob-t7-portal-mm-tomcat/admin/styles/admin-dashboard.css` | `templates/app/admin/styles/admin-dashboard.css` |
| `/uob-t7-portal-mm-tomcat/admin/scripts/admin-dashboard.js` | `templates/app/admin/scripts/admin-dashboard.js` |

**Note:** Turbine/Velocity may handle URL routing differently. Check `web.xml` or `TurbineResources.properties` for URL mapping configuration.

---

## Troubleshooting

### If Files Not Found:

1. **Check if WAR is extracted:**
   ```powershell
   Test-Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat"
   ```

2. **If directory doesn't exist:**
   - Restart Tomcat to extract WAR
   - Or manually extract WAR file

3. **If files are old:**
   - Rebuild: `mvn clean package`
   - Redeploy WAR
   - Restart Tomcat

4. **Check WAR file contents:**
   - WAR files are ZIP archives
   - Can be opened with 7-Zip or WinRAR
   - Check if files exist inside WAR before deployment

---

## Quick Reference Commands

```powershell
# Run diagnostic script
cd C:\Turbineprojects\uob-turbine7-portal-mm
.\check-ui-changes.ps1

# Check all admin files
Get-ChildItem "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\admin" -Recurse | Select-Object FullName

# Check template files
Get-ChildItem "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\templates\app\screens\admin" -Recurse | Select-Object FullName
```

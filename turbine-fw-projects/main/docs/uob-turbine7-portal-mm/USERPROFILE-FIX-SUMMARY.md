# UserProfile Screen Not Working - Diagnosis and Solution

## Problem
The UserProfile page is accessible but shows no data (no roles, groups, permissions). The template is being rendered but the Java screen class is not being executed.

## Root Cause Analysis

1. **Screen Class Exists**: `UserProfile.class` is compiled and included in the JAR at `com/uob/modules/screens/UserProfile.class`
2. **Template Exists**: `UserProfile.vm` template is at `templates/app/screens/UserProfile.vm`
3. **Template is Being Accessed**: Logs show `template -> UserProfile.vm` is being requested
4. **Screen Class NOT Being Called**: No "UserProfile: Loading profile" log messages appear

## Issue
When using `setPage("UserProfile.vm")`, Turbine is finding and rendering the template file directly without invoking the corresponding screen class `UserProfile`.

## Solution Options

### Option 1: Move Template to Root Level (Recommended)
Move `UserProfile.vm` from `templates/app/screens/` to `templates/app/` so it's treated as a page template that requires a screen class.

**Steps:**
1. Move `templates/app/screens/UserProfile.vm` → `templates/app/UserProfile.vm`
2. Update links to use `setPage("UserProfile.vm")` (no change needed)
3. Rebuild and redeploy

### Option 2: Use Explicit Screen Parameter
Instead of `setPage()`, use URL parameter `screen=UserProfile`:
- Change links to: `$link.addPathInfo("screen", "UserProfile")`
- Or use: `?screen=UserProfile` in URL

### Option 3: Check Template Service Configuration
Verify that the TemplateService is configured to use ClassMapper for screens, which should automatically map template names to screen classes.

## Current Status

✅ **Completed:**
- UserProfile.java screen class created with enhanced logging
- Template UserProfile.vm created with hierarchical display
- SQL script created to assign roles to all users
- All users have roles/groups/permissions assigned in database
- Screen class is compiled and in JAR

❌ **Issue:**
- Screen class is not being invoked when template is accessed
- Template is rendered directly without screen class execution

## Next Steps

1. **Try Option 1** (Move template):
   ```powershell
   Move-Item webapp\src\main\webapp\templates\app\screens\UserProfile.vm webapp\src\main\webapp\templates\app\UserProfile.vm
   ```

2. **Rebuild and Deploy**:
   ```powershell
   mvn clean install -DskipTests
   Copy-Item webapp\target\uob-t7-portal-mm-tomcat.war C:\applicationservers\apache-tomcat-10.1.44\webapps\
   ```

3. **Restart Tomcat and Test**

4. **Check Logs** for "UserProfile.doBuildTemplate() CALLED!" message

## Verification

After fix, you should see in logs:
```
UserProfile.doBuildTemplate() CALLED!
UserProfile: Loading profile for user: [username]
UserProfile: ACL retrieved: Yes
UserProfile: User [username] has roles in group: [groupname]
```

And the page should display:
- User information
- Groups with roles
- Roles with permissions
- Hierarchical view

# UserProfile Authorization Fix

## Problem
User was seeing "You do not have access to this part of the site" message when trying to access the UserProfile page.

## Root Cause
The `UserProfile` screen class extends `SecureScreen`, which has a strict `isAuthorized()` method that requires users to have either `turbineuser` or `turbineadmin` roles. Additionally, the screen class was not being invoked at all, causing the template to render without data.

## Solution Applied
1. **Added `isAuthorized()` override** in `UserProfile.java` to:
   - Allow all logged-in users to view their profile
   - Add logging to verify screen class is being invoked
   - Fall back to parent class authorization if user is not logged in

2. **Enhanced logging** in both `isAuthorized()` and `doBuildTemplate()` methods to help diagnose if the screen class is being invoked.

## Changes Made

### File: `turbine-model-controller/src/main/java/modules/screens/UserProfile.java`

Added method:
```java
@Override
protected boolean isAuthorized(PipelineData pipelineData) throws Exception {
    log.info("========================================");
    log.info("UserProfile.isAuthorized() CALLED!");
    log.info("========================================");
    
    RunData data = (RunData) pipelineData;
    User user = data.getUser();
    
    if (user != null && user.hasLoggedIn()) {
        log.info("UserProfile: User " + user.getName() + " is authorized");
        return true;
    }
    
    log.warn("UserProfile: User not logged in or null");
    return super.isAuthorized(pipelineData);
}
```

## Next Steps

1. **Restart Tomcat** to load the new WAR file
2. **Access UserProfile page** and check logs for:
   - `UserProfile.isAuthorized() CALLED!` - confirms screen class is invoked
   - `UserProfile.doBuildTemplate() CALLED!` - confirms template building is invoked
3. **If logs don't appear**, the screen class is still not being discovered/invoked by Turbine, which is a framework discovery issue that needs further investigation.

## Verification

After restarting Tomcat, check the logs:
```powershell
# Check for screen class invocation
Select-String -Path "C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal-mm-tomcat\logs\*.log" -Pattern "UserProfile\.(isAuthorized|doBuildTemplate)" | Select-Object -Last 10
```

Expected log entries:
- `UserProfile.isAuthorized() CALLED!`
- `UserProfile: User <username> is authorized`
- `UserProfile.doBuildTemplate() CALLED!`
- `UserProfile: Loading profile for user: <username>`

## Notes

- The authorization fix allows all logged-in users to view their profile
- If you need stricter authorization, modify the `isAuthorized()` method in `UserProfile.java`
- The logging will help diagnose if the screen class discovery mechanism is working

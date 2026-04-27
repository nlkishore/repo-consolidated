# Velocity Template Rendering Error Fix

## Problem
When accessing the UserProfile page, the following error occurred:
```
Error rendering Velocity template: screens/UserProfile.vm
org.apache.velocity.exception.ResourceNotFoundException: Unable to find resource 'app/layouts/Default.vm'
```

## Root Cause
The `UserProfile.vm` template was trying to manually parse the layout file using:
```velocity
#parse ("app/layouts/Default.vm")
```

However, in Apache Turbine, **screen templates should NOT manually parse layout files**. The Turbine framework automatically applies the layout to screen templates. The layout is configured in `TurbineResources.properties` and is applied by the framework's layout module.

## Solution
Removed the `#parse ("app/layouts/Default.vm")` line from `UserProfile.vm`.

### File Changed
- `webapp/src/main/webapp/templates/app/screens/UserProfile.vm`

### Change Made
**Before:**
```velocity
#*

 * Licensed to the Apache Software Foundation (ASF) under one
 * ...
 *#

#parse ("app/layouts/Default.vm")

<div class="w3-container w3-padding-16">
```

**After:**
```velocity
#*

 * Licensed to the Apache Software Foundation (ASF) under one
 * ...
 *#

<div class="w3-container w3-padding-16">
```

## Verification
After this fix:
1. ✅ Screen class is being invoked (confirmed by logs showing `UserProfile.doBuildTemplate() CALLED!`)
2. ✅ Template should now render without the ResourceNotFoundException
3. ✅ Layout will be automatically applied by Turbine framework

## Notes
- Other screen templates (Index.vm, Login.vm, Password.vm, Error.vm) do NOT use `#parse` for layouts
- Turbine's layout system automatically wraps screen content with the configured layout
- The layout file `app/layouts/Default.vm` exists and is used by the framework, but should not be manually parsed by screen templates

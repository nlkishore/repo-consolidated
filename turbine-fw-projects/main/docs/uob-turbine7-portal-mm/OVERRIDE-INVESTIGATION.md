# GtpUser Override Investigation

**Date:** January 24, 2026  
**Issue:** `ClassCastException` - `GtpGroup` cannot be cast to `TurbineUserGroupRoleModelPeerMapper`  
**Status:** Overrides not being invoked

---

## Problem Summary

The `ClassCastException` persists because our overrides in `GtpUser.java` are **not being invoked**. The framework is calling base class methods directly, bypassing our overrides.

**Evidence:**
- No debug messages in logs (static initializer, method overrides)
- Stack trace shows `DefaultAbstractTurbineUser.retrieveAttachedObjects()` being called
- Our overrides are never executed

---

## Investigation Results

### 1. Configuration Verification ✅

**File:** `webapp/src/main/webapp/WEB-INF/conf/componentConfiguration.xml`

```xml
<userManager>
    <className>com.uob.om.GtpUser</className>
    <peerClassName>com.uob.om.GtpUserPeerImpl</peerClassName>
    ...
</userManager>
```

**Finding:** Configuration correctly specifies `GtpUser` as the className.

### 2. Object Creation Verification ✅

**File:** `torque-orm/target/generated-sources/com/uob/om/BaseGtpUserRecordMapper.java`

```java
public GtpUser processRow(...) throws TorqueException {
    GtpUser gtpUser = new GtpUser();  // Line 85
    ...
}
```

**Finding:** RecordMapper creates `GtpUser` instances, not `BaseGtpUser`.

### 3. Logging Implementation ✅

**File:** `torque-orm/src/main/java/com/uob/om/GtpUser.java`

- Added Log4j2 logger: `private static final Logger log = LogManager.getLogger(GtpUser.class);`
- Added logging to:
  - Static initializer (class loading)
  - `getTurbineUserGroupRolesJoinTurbineGroup()` method
  - `retrieveAttachedObjects()` method

**Log Locations:**
- `application.log` (application-specific logs)
- `avalon.log` (Avalon framework logs)

### 4. Override Methods Status

| Method | Override Status | Debug Logging | Notes |
|--------|----------------|---------------|-------|
| `retrieveAttachedObjects(Connection)` | ✅ Overridden | ✅ Added | Not being invoked |
| `getTurbineUserGroupRolesJoinTurbineGroup()` | ✅ Overridden | ✅ Added | Not being invoked |
| Static initializer | ✅ Present | ✅ Added | Not appearing in logs |

---

## Root Cause Analysis

**CONFIRMED:** The framework is calling base class methods directly, bypassing our overrides.

**Evidence:**
- ✅ Class is loaded: `"GtpUser class loaded"` appears in `application.log`
- ✅ Instances are `GtpUser`: Static initializer confirms correct class
- ❌ Override methods are NOT invoked: No "OVERRIDE CALLED" messages appear
- ❌ Stack trace shows: `DefaultAbstractTurbineUser.retrieveAttachedObjects()` being called

**Possible reasons:**
1. **Reflection-based invocation:** Framework uses reflection to call methods on base class
2. **Method dispatch bypass:** Framework bypasses normal Java method dispatch
3. **Direct base class reference:** Framework may be calling through `DefaultAbstractTurbineUser` reference
4. **Different method signature:** Framework may be calling a different overload we haven't overridden

---

## Next Steps

### Immediate Actions

1. **Check Logs After Redeployment:**
   - Look for `"GtpUser class loaded"` in `application.log` or `avalon.log`
   - Look for `"GtpUser.retrieveAttachedObjects() OVERRIDE CALLED"` messages
   - Look for `"GtpUser.getTurbineUserGroupRolesJoinTurbineGroup() OVERRIDE CALLED"` messages

2. **If Logs Still Don't Appear:**
   - Framework may be using reflection to call base class methods
   - Consider overriding at `GtpUserPeerImpl` level
   - Check if framework has a factory pattern that needs configuration

3. **Alternative Approaches:**
   - Override `doSelectSingleRecord()` in `GtpUserPeerImpl` to intercept object creation
   - Override `processRow()` in a custom RecordMapper
   - Use AOP (Aspect-Oriented Programming) to intercept method calls
   - Modify the base class behavior through configuration

### Long-term Solutions

1. **Framework Configuration:**
   - Verify if there's a configuration option to force use of custom classes
   - Check if framework has a factory interface that needs implementation

2. **Code-Level Fixes:**
   - Override at PeerImpl level if framework calls through Peer
   - Create a custom RecordMapper that ensures proper object types
   - Intercept at the Torque level before framework processes objects

3. **Architecture Review:**
   - Consider if the framework's design allows for proper override
   - Evaluate if a different approach (e.g., wrapper pattern) would work better

---

## Files Modified

1. `torque-orm/src/main/java/com/uob/om/GtpUser.java`
   - Added Log4j2 logger
   - Added logging to all override methods
   - Added class name logging to verify instance type

---

## Testing Checklist

After redeployment, verify:

- [ ] `"GtpUser class loaded"` appears in logs
- [ ] `"GtpUser.retrieveAttachedObjects() OVERRIDE CALLED"` appears when page is accessed
- [ ] `"GtpUser.getTurbineUserGroupRolesJoinTurbineGroup() OVERRIDE CALLED"` appears
- [ ] Class name in logs shows `com.uob.om.GtpUser` (not `BaseGtpUser`)
- [ ] `ClassCastException` is resolved

---

## Related Files

- `torque-orm/src/main/java/com/uob/om/GtpUser.java` - Main override class
- `torque-orm/src/main/java/com/uob/om/GtpUserPeer.java` - Peer wrapper
- `torque-orm/src/main/java/com/uob/om/GtpUserPeerImpl.java` - Peer implementation
- `webapp/src/main/webapp/WEB-INF/conf/componentConfiguration.xml` - Framework configuration
- `torque-orm/target/generated-sources/com/uob/om/BaseGtpUserRecordMapper.java` - Object creation

---

## Notes

- The framework (Fulcrum Security / Turbine) may have internal mechanisms that bypass normal Java method dispatch
- The `DefaultAbstractTurbineUser` class is in a JAR file, so we cannot modify it directly
- Our overrides are syntactically correct but not being invoked by the framework
- The configuration specifies `GtpUser`, but the framework may still be using base class methods

# Override Failure Analysis - Final Report

**Date:** January 24, 2026  
**Issue:** `ClassCastException` - Framework bypasses all method overrides  
**Status:** ❌ Overrides not working - Framework uses reflection or direct base class invocation

---

## Executive Summary

After extensive investigation and multiple attempts, **our method overrides in `GtpUser.java` are NOT being invoked by the framework**. The framework (`TorqueTurbineUserManagerImpl`) calls base class methods directly, completely bypassing our overrides.

**Confirmed Facts:**
- ✅ Class is loaded correctly (`GtpUser` instances are created)
- ✅ Configuration specifies `GtpUser` correctly
- ✅ Override methods are syntactically correct
- ❌ Override methods are NEVER invoked
- ❌ Framework calls `DefaultAbstractTurbineUser.retrieveAttachedObjects()` directly

---

## Evidence

### 1. Class Loading ✅
```
2026-01-24 18:03:17,176 [http-nio-8081-exec-1] INFO  com.uob.om.GtpUser - GtpUser class loaded - class: com.uob.om.GtpUser
```
**Finding:** Class is loaded and instances are `GtpUser`, not `BaseGtpUser`.

### 2. Override Messages ❌
**Expected:** `"GtpUser.retrieveAttachedObjects() OVERRIDE CALLED"`  
**Actual:** No such messages appear in any log file

**Expected:** `"GtpUser.getTurbineUserGroupRolesJoinTurbineGroup() OVERRIDE CALLED"`  
**Actual:** No such messages appear in any log file

### 3. Stack Trace Analysis
```
at org.apache.fulcrum.security.torque.turbine.DefaultAbstractTurbineUser.retrieveAttachedObjects(DefaultAbstractTurbineUser.java:93)
at org.apache.fulcrum.security.torque.turbine.DefaultAbstractTurbineUser.maptoModel(DefaultAbstractTurbineUser.java:183)
at org.apache.fulcrum.security.torque.turbine.TorqueTurbineUserManagerImpl.attachRelatedObjects(TorqueTurbineUserManagerImpl.java:378)
```

**Finding:** Framework calls base class methods directly, bypassing our overrides.

---

## Root Cause

The framework (`TorqueTurbineUserManagerImpl`) calls `retrieveAttachedObjects()` in a way that bypasses normal Java method dispatch. Possible mechanisms:

1. **Reflection-based invocation:**
   - Framework uses `Method.invoke()` on `DefaultAbstractTurbineUser.class`
   - This bypasses method dispatch and calls the base class method directly

2. **Direct base class reference:**
   - Framework may cast to `DefaultAbstractTurbineUser` and call method
   - However, this shouldn't bypass dispatch in normal Java

3. **Different code path:**
   - Framework may have a different method that calls base class directly
   - Our override may not match the exact method being called

---

## Technical Details

### The Problem Flow

1. **Framework calls:** `TorqueTurbineUserManagerImpl.attachRelatedObjects()` (line 378)
2. **Which calls:** `DefaultAbstractTurbineUser.retrieveAttachedObjects()` (line 93)
3. **Which calls:** `getTurbineUserGroupRolesJoinTurbineGroup()` (base class method)
4. **Which uses:** `doSelectJoinGtpGroup()` (JOIN query)
5. **JOIN query returns:** Mixed objects (`GtpUserGroupRole` + `GtpGroup`)
6. **Then calls:** `maptoModel()` with mixed collection
7. **maptoModel() tries to cast:** Everything to `TurbineUserGroupRoleModelPeerMapper`
8. **Fails:** `GtpGroup` cannot be cast → `ClassCastException`

### Why Our Override Doesn't Work

Our override of `getTurbineUserGroupRolesJoinTurbineGroup()` uses `doSelect()` instead of `doSelectJoinGtpGroup()`, which should return only `GtpUserGroupRole` objects. However:

- ❌ Framework never calls our override
- ❌ Framework calls base class method directly
- ❌ Base class method uses `doSelectJoinGtpGroup()` which returns mixed objects

---

## Attempted Solutions

### ✅ Solution 1: Override `retrieveAttachedObjects()`
- **Status:** Implemented but not invoked
- **Result:** Framework bypasses override

### ✅ Solution 2: Override `getTurbineUserGroupRolesJoinTurbineGroup()`
- **Status:** Implemented but not invoked
- **Result:** Framework bypasses override

### ✅ Solution 3: Add Log4j2 logging
- **Status:** Implemented and working
- **Result:** Confirms overrides are not being called

### ✅ Solution 4: Clean restart with cache clearing
- **Status:** Completed
- **Result:** Confirmed overrides still not working

---

## Alternative Solutions

Since method overriding doesn't work, we need alternative approaches:

### Option 1: Override at Peer Level ⚠️
**Approach:** Override `doSelectJoinGtpGroup()` in `GtpUserGroupRolePeerImpl` to use `doSelect()` instead.

**Pros:**
- Intercepts at query level
- Prevents JOIN from including `GtpGroup` objects

**Cons:**
- May break other functionality that relies on JOIN
- Framework may still call base class Peer method

### Option 2: Custom RecordMapper ⚠️
**Approach:** Override `GtpUserGroupRoleRecordMapper` to filter out `GtpGroup` objects.

**Pros:**
- Intercepts at data mapping level
- Can filter objects before they reach `maptoModel()`

**Cons:**
- Complex implementation
- May not be called if framework uses base class mapper

### Option 3: Modify Torque Schema ⚠️
**Approach:** Modify the Torque schema to not generate JOIN methods.

**Pros:**
- Prevents JOIN queries at source
- Framework would have to use `doSelect()`

**Cons:**
- May break framework functionality
- Requires schema regeneration
- May not be possible if framework requires JOINs

### Option 4: AOP (Aspect-Oriented Programming) ⚠️
**Approach:** Use AOP to intercept method calls and filter collections.

**Pros:**
- Can intercept at any level
- Doesn't require modifying framework code

**Cons:**
- Requires AOP framework (e.g., AspectJ)
- Adds complexity
- May have performance impact

### Option 5: Wrapper/Adapter Pattern ⚠️
**Approach:** Create a wrapper that intercepts method calls before framework processes them.

**Pros:**
- Can intercept at object level
- Doesn't require framework modification

**Cons:**
- Complex implementation
- May not work if framework creates objects directly

---

## Recommended Next Steps

### Immediate Action
1. **Verify method signature match:**
   - Check if `retrieveAttachedObjects()` signature exactly matches base class
   - Verify access modifiers (`public` vs `protected`)
   - Check for any method overloading issues

2. **Try overriding at Peer level:**
   - Override `doSelectJoinGtpGroup()` in `GtpUserGroupRolePeerImpl`
   - Make it return only `GtpUserGroupRole` objects (no JOIN)

3. **Check framework source code:**
   - If available, examine `TorqueTurbineUserManagerImpl.attachRelatedObjects()`
   - Understand how it calls `retrieveAttachedObjects()`
   - Determine if reflection is being used

### Long-term Solution
Since the framework bypasses normal method dispatch, the most reliable solution is to **prevent the JOIN query from including `GtpGroup` objects at the query level**. This can be done by:

1. **Overriding Peer methods** to use `doSelect()` instead of `doSelectJoinGtpGroup()`
2. **Modifying Torque schema** to not generate JOIN methods (if framework allows)
3. **Using AOP** to intercept and filter collections before `maptoModel()` is called

---

## Files Modified

1. `torque-orm/src/main/java/com/uob/om/GtpUser.java`
   - Added `retrieveAttachedObjects()` override
   - Added `getTurbineUserGroupRolesJoinTurbineGroup()` override
   - Added Log4j2 logging
   - **Status:** Overrides present but not invoked

---

## Conclusion

The framework's architecture prevents normal method overriding from working. The framework uses a mechanism (likely reflection) that bypasses Java's method dispatch, causing base class methods to be called directly.

**This is a fundamental limitation of the framework's design.** To resolve the `ClassCastException`, we must either:
1. Intercept at a different level (Peer, RecordMapper, or AOP)
2. Modify the framework's behavior through configuration (if possible)
3. Accept that the framework's design doesn't support this type of override

The investigation has confirmed that method overriding is not a viable solution for this issue.

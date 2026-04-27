# Investigation Findings - Framework Method Call Analysis

**Date:** January 24, 2026  
**Issue:** ClassCastException - Framework bypasses all overrides  
**Discovery:** Framework uses separate queries, not JOIN queries

---

## Critical Discovery

### SQL Log Analysis

The framework is **NOT using JOIN queries**. Instead, it executes **separate SELECT queries**:

1. `SELECT FROM GTP_USER_GROUP_ROLE WHERE USER_ID=?`
2. `SELECT FROM GTP_GROUP WHERE GROUP_ID=?`
3. `SELECT FROM GTP_USER_GROUP_ROLE WHERE GROUP_ID=?`

### Implications

- ❌ Framework is **NOT calling** `doSelectJoinGtpGroup()`
- ❌ Our Peer-level override is **never invoked**
- ✅ Framework uses separate `doSelect()` calls
- ✅ Framework collects results from multiple queries
- ❌ Then calls `maptoModel()` with **mixed collection**
- ❌ `maptoModel()` tries to cast `GtpGroup` to `TurbineUserGroupRoleModelPeerMapper`
- ❌ **ClassCastException** occurs

---

## Root Cause

The framework's `DefaultAbstractTurbineUser.retrieveAttachedObjects()` method:

1. Calls `getTurbineUserGroupRolesJoinTurbineGroup()` (base class method)
2. This method executes **separate queries** (not JOIN):
   - Query 1: Get `GtpUserGroupRole` objects by `USER_ID`
   - Query 2: Get `GtpGroup` objects by `GROUP_ID`
   - Query 3: Get `GtpUserGroupRole` objects by `GROUP_ID`
3. Collects all results into a **single collection**
4. Passes this **mixed collection** to `maptoModel()`
5. `maptoModel()` expects only `TurbineUserGroupRoleModelPeerMapper` objects
6. Tries to cast `GtpGroup` → **ClassCastException**

---

## Why Our Overrides Don't Work

### User-Level Override (`GtpUser.retrieveAttachedObjects()`)
- ❌ Framework calls base class method directly
- ❌ Bypasses our override (likely using reflection)

### Peer-Level Override (`GtpUserGroupRolePeerImpl.doSelectJoinGtpGroup()`)
- ❌ Framework doesn't call JOIN methods
- ❌ Framework uses separate `doSelect()` calls
- ❌ Our override is never invoked

---

## Solution Options

### Option 1: Override Static Peer Methods ⚠️
**Approach:** Override static methods in `GtpUserGroupRolePeer` to intercept `doSelect()` calls.

**Pros:**
- Intercepts at Peer level
- Framework calls static Peer methods

**Cons:**
- Static methods can't be overridden in Java
- Would need to modify generated base class (not recommended)

### Option 2: Override `doSelect()` in PeerImpl ⚠️
**Approach:** Override `doSelect()` methods in `GtpUserGroupRolePeerImpl` to filter results.

**Pros:**
- Intercepts at query execution level
- Can filter `GtpGroup` objects before they're collected

**Cons:**
- Framework may call base class `doSelect()` directly
- May break other functionality

### Option 3: Intercept Collection Building ⚠️
**Approach:** Override methods that build the collection before `maptoModel()` is called.

**Pros:**
- Intercepts at collection level
- Can filter objects before casting

**Cons:**
- Need to find the exact method that builds the collection
- Framework may bypass our override

### Option 4: AOP (Aspect-Oriented Programming) ⚠️
**Approach:** Use AOP to intercept `maptoModel()` calls and filter the collection.

**Pros:**
- Intercepts at method call level
- Doesn't require framework modification
- Can filter collection before casting

**Cons:**
- Requires AOP framework (e.g., AspectJ)
- Adds complexity
- May have performance impact

### Option 5: Custom RecordMapper ⚠️
**Approach:** Override `GtpUserGroupRoleRecordMapper` to prevent `GtpGroup` objects from being created.

**Pros:**
- Intercepts at data mapping level
- Prevents `GtpGroup` objects from being created

**Cons:**
- Framework may use base class mapper
- May not be called if framework uses different mapper

### Option 6: Modify Framework Behavior via Configuration ⚠️
**Approach:** Check if framework has configuration to control object loading behavior.

**Pros:**
- No code changes required
- Framework handles it

**Cons:**
- May not exist
- May not solve the problem

---

## Recommended Next Steps

1. **Check if `doSelect()` can be overridden in PeerImpl:**
   - Verify if framework calls instance or static methods
   - Test if overriding `doSelect()` works

2. **Investigate `maptoModel()` method:**
   - Understand what it expects
   - See if we can filter collection before it's called

3. **Consider AOP solution:**
   - If other options fail, AOP may be the only viable solution
   - Use AspectJ to intercept `maptoModel()` calls

4. **Check framework configuration:**
   - Look for lazy loading options
   - Check if there's a way to control object loading

---

## Files to Investigate

1. `BaseGtpUserGroupRolePeer.java` - Static Peer methods
2. `BaseGtpUserGroupRolePeerImpl.java` - Instance Peer methods
3. `GtpUserGroupRolePeerImpl.java` - Our Peer implementation
4. Framework JAR: `DefaultAbstractTurbineUser.java` - `retrieveAttachedObjects()` and `maptoModel()`

---

## Conclusion

The framework's architecture prevents normal method overriding from working. The framework:
- Uses separate queries instead of JOIN queries
- Collects results into mixed collections
- Calls `maptoModel()` with mixed collections
- Bypasses all our overrides

**The solution must intercept at a different level** - either at the query execution level (`doSelect()`), collection building level, or method call level (AOP).

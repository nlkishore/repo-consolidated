# Solution Options for ClassCastException

**Date:** January 24, 2026  
**Issue:** `ClassCastException` - `GtpGroup` cannot be cast to `TurbineUserGroupRoleModelPeerMapper`  
**Root Cause:** Framework collects objects from separate queries into mixed collection, then passes to `maptoModel()`

---

## Investigation Summary

### Key Findings

1. **Framework uses separate queries (NOT JOIN):**
   - `SELECT FROM GTP_USER_GROUP_ROLE WHERE USER_ID=?`
   - `SELECT FROM GTP_GROUP WHERE GROUP_ID=?`
   - `SELECT FROM GTP_USER_GROUP_ROLE WHERE GROUP_ID=?`

2. **Framework collects results into mixed collection:**
   - Contains both `GtpUserGroupRole` and `GtpGroup` objects
   - Passes this collection to `maptoModel()`

3. **`maptoModel()` expects only `TurbineUserGroupRoleModelPeerMapper` objects:**
   - Tries to cast `GtpGroup` → **ClassCastException**

4. **Our overrides are NOT being called:**
   - User-level override (`GtpUser.retrieveAttachedObjects()`) - bypassed
   - Peer-level override (`GtpUserGroupRolePeerImpl.doSelectJoinGtpGroup()`) - never called (framework doesn't use JOIN)

---

## Solution Options

### Option 1: AOP (Aspect-Oriented Programming) ⭐ **RECOMMENDED**

**Approach:** Use AspectJ to intercept `maptoModel()` calls and filter the collection.

**Implementation:**
```java
@Aspect
public class MaptoModelInterceptor {
    @Around("execution(* org.apache.fulcrum.security.torque.turbine.DefaultAbstractTurbineUser.maptoModel(..))")
    public Object filterCollection(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Collection) {
            Collection<?> collection = (Collection<?>) args[0];
            // Filter out GtpGroup objects
            List<Object> filtered = collection.stream()
                .filter(obj -> obj instanceof TurbineUserGroupRoleModelPeerMapper)
                .collect(Collectors.toList());
            args[0] = filtered;
        }
        return joinPoint.proceed(args);
    }
}
```

**Pros:**
- ✅ Intercepts at method call level
- ✅ Doesn't require framework modification
- ✅ Can filter collection before casting
- ✅ Works regardless of framework's internal implementation

**Cons:**
- ⚠️ Requires AspectJ dependency
- ⚠️ Adds complexity
- ⚠️ May have slight performance impact

**Dependencies:**
```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.20</version>
</dependency>
```

---

### Option 2: Override `doSelect()` in `GtpGroupPeerImpl` ⚠️

**Approach:** Override `doSelect()` to return empty list when called from `retrieveAttachedObjects()` context.

**Implementation:**
```java
public class GtpGroupPeerImpl extends BaseGtpGroupPeerImpl {
    @Override
    public List<GtpGroup> doSelect(Criteria criteria, Connection conn) throws TorqueException {
        // Check if called from retrieveAttachedObjects context
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        boolean fromRetrieveAttached = Arrays.stream(stack)
            .anyMatch(e -> e.getMethodName().equals("retrieveAttachedObjects"));
        
        if (fromRetrieveAttached) {
            log.warn("Preventing GtpGroup query from retrieveAttachedObjects context");
            return new ArrayList<>();
        }
        return super.doSelect(criteria, conn);
    }
}
```

**Pros:**
- ✅ Prevents `GtpGroup` objects from being loaded
- ✅ No AOP dependency required

**Cons:**
- ⚠️ May break other functionality that needs `GtpGroup
- ⚠️ Stack trace inspection is fragile
- ⚠️ May not work if framework uses different code path

---

### Option 3: Custom `TurbineUserGroupRoleModelPeerMapper` Implementation ⚠️

**Approach:** Make `GtpGroup` implement `TurbineUserGroupRoleModelPeerMapper` interface.

**Implementation:**
```java
public class GtpGroup extends BaseGtpGroup implements TurbineUserGroupRoleModelPeerMapper {
    // Implement interface methods
}
```

**Pros:**
- ✅ Prevents ClassCastException
- ✅ No framework modification

**Cons:**
- ⚠️ Semantically incorrect (GtpGroup is not a UserGroupRole)
- ⚠️ May cause other issues
- ⚠️ Violates object-oriented principles

---

### Option 4: Modify Framework Configuration ⚠️

**Approach:** Check if framework has configuration to control object loading behavior.

**Possible configurations:**
- Lazy loading options
- Object loading strategies
- Collection building behavior

**Pros:**
- ✅ No code changes if configuration exists

**Cons:**
- ⚠️ May not exist
- ⚠️ May not solve the problem
- ⚠️ Requires framework documentation

---

### Option 5: Fork and Modify Framework JAR ⚠️ **NOT RECOMMENDED**

**Approach:** Fork the framework, modify `DefaultAbstractTurbineUser.maptoModel()`, and use custom JAR.

**Pros:**
- ✅ Direct solution

**Cons:**
- ❌ Maintenance burden
- ❌ Version compatibility issues
- ❌ Not recommended for production

---

## Recommended Solution: AOP (Option 1)

**Why AOP is the best solution:**
1. **Non-invasive:** Doesn't require framework modification
2. **Reliable:** Works regardless of framework's internal implementation
3. **Maintainable:** Clear separation of concerns
4. **Flexible:** Can be easily modified or removed

**Implementation Steps:**
1. Add AspectJ dependency to `webapp/pom.xml`
2. Create `MaptoModelInterceptor` class
3. Configure AspectJ in Spring configuration
4. Test and verify

---

## Alternative: Quick Fix (Option 2)

If AOP is not feasible, Option 2 (override `doSelect()` in `GtpGroupPeerImpl`) can be implemented quickly, but it's less reliable and may break other functionality.

---

## Next Steps

1. **Implement AOP solution (Option 1):**
   - Add AspectJ dependency
   - Create interceptor class
   - Configure AspectJ
   - Test

2. **If AOP doesn't work, try Option 2:**
   - Override `doSelect()` in `GtpGroupPeerImpl`
   - Add stack trace inspection
   - Test carefully

3. **Document the solution:**
   - Update README with AOP configuration
   - Document any limitations

---

## Files to Modify

1. `webapp/pom.xml` - Add AspectJ dependency
2. `turbine-model-controller/src/main/java/com/uob/aspect/MaptoModelInterceptor.java` - Create interceptor
3. `webapp/src/main/webapp/WEB-INF/conf/spring-config.xml` - Configure AspectJ (if using Spring AOP)

---

## Conclusion

**AOP (Option 1) is the recommended solution** because it:
- Intercepts at the right level (method call)
- Doesn't require framework modification
- Is maintainable and flexible
- Works regardless of framework's internal implementation

The alternative (Option 2) can be used as a quick fix, but it's less reliable and may have side effects.

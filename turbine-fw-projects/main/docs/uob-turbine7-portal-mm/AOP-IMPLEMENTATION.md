# AOP Implementation Guide

**Date:** January 24, 2026  
**Purpose:** Implement AspectJ Load-Time Weaving to fix ClassCastException

---

## Overview

This document describes the AOP (Aspect-Oriented Programming) solution implemented to fix the `ClassCastException` caused by `GtpGroup` objects being included in collections passed to `maptoModel()`.

---

## Implementation Details

### 1. AspectJ Dependency

**File:** `webapp/pom.xml`

Added AspectJ weaver dependency:
```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.20</version>
</dependency>
```

### 2. AOP Interceptor Class

**File:** `turbine-model-controller/src/main/java/com/uob/aspect/MaptoModelInterceptor.java`

This aspect intercepts calls to `DefaultAbstractTurbineUser.maptoModel()` and filters the collection to remove non-mapper objects before they cause `ClassCastException`.

**Key Features:**
- Intercepts `maptoModel()` method calls
- Filters collection to only include `TurbineUserGroupRoleModelPeerMapper` objects
- Logs filtering activity for debugging
- Prevents `ClassCastException` by removing `GtpGroup` objects

### 3. AspectJ Load-Time Weaving Configuration

**File:** `webapp/src/main/resources/META-INF/aop.xml`

Configures AspectJ Load-Time Weaving (LTW) to weave the aspect into the framework classes at runtime.

**Configuration:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<aspectj>
    <aspects>
        <aspect name="com.uob.aspect.MaptoModelInterceptor"/>
    </aspects>
    
    <weaver>
        <include within="org.apache.fulcrum.security.torque.turbine.DefaultAbstractTurbineUser"/>
        <include within="com.uob.aspect..*"/>
    </weaver>
</aspectj>
```

### 4. Spring Configuration

**File:** `spring-rest-api/src/main/java/com/uob/config/SpringConfig.java`

Added `@EnableAspectJAutoProxy` annotation to enable AspectJ support (though LTW works independently).

---

## Deployment Configuration

### AspectJ Load-Time Weaving (LTW)

AspectJ LTW requires a Java agent to be specified when starting the JVM. This can be done in two ways:

#### Option 1: JVM Argument (Recommended for Development)

Add to Tomcat startup script (`setenv.bat` or `catalina.bat`):

```batch
set JAVA_OPTS=%JAVA_OPTS% -javaagent:C:\path\to\aspectjweaver-1.9.20.jar
```

Or find the aspectjweaver JAR in your Maven repository:
```
%USERPROFILE%\.m2\repository\org\aspectj\aspectjweaver\1.9.20\aspectjweaver-1.9.20.jar
```

#### Option 2: Copy JAR to Tomcat lib Directory

1. Copy `aspectjweaver-1.9.20.jar` to `$CATALINA_HOME/lib/`
2. Add to `$CATALINA_HOME/bin/setenv.bat`:
   ```batch
   set JAVA_OPTS=%JAVA_OPTS% -javaagent:%CATALINA_HOME%\lib\aspectjweaver-1.9.20.jar
   ```

#### Option 3: Use Maven to Copy JAR

Add to `webapp/pom.xml`:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-aspectjweaver</id>
            <phase>package</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjweaver</artifactId>
                        <version>1.9.20</version>
                        <outputDirectory>${project.build.directory}/aspectj-lib</outputDirectory>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## How It Works

1. **AspectJ LTW Agent:** When Tomcat starts with the AspectJ weaver agent, it instruments classes as they are loaded.

2. **Method Interception:** When `DefaultAbstractTurbineUser.maptoModel()` is called, our aspect intercepts the call.

3. **Collection Filtering:** The aspect checks if the first argument is a Collection and filters out any objects that are not `TurbineUserGroupRoleModelPeerMapper` instances.

4. **Proceed with Filtered Collection:** The filtered collection is passed to the original `maptoModel()` method, preventing `ClassCastException`.

---

## Verification

After deployment, check logs for:

1. **AspectJ LTW Initialization:**
   ```
   [AspectJ] Load-time weaving enabled
   ```

2. **Interceptor Messages:**
   ```
   MaptoModelInterceptor: Intercepting maptoModel() call
   MaptoModelInterceptor: Processing collection with X object(s)
   MaptoModelInterceptor: Filtered Y non-mapper object(s) from collection
   ```

3. **No ClassCastException:**
   - Check `avalon.log` for absence of `ClassCastException` errors
   - Application should load pages successfully

---

## Troubleshooting

### AspectJ LTW Not Working

**Symptoms:**
- No interceptor messages in logs
- `ClassCastException` still occurs

**Solutions:**
1. Verify JVM argument is set: `-javaagent:path/to/aspectjweaver.jar`
2. Check `aop.xml` is in `WEB-INF/classes/META-INF/` in the WAR
3. Verify aspect class is in the classpath
4. Check AspectJ version compatibility

### Aspect Not Being Called

**Symptoms:**
- No interceptor messages in logs
- AspectJ LTW is enabled

**Solutions:**
1. Verify pointcut expression matches the method signature
2. Check that the target class is included in `aop.xml` weaver configuration
3. Ensure aspect class is annotated with `@Aspect`
4. Verify aspect is in the classpath

### Filtering Not Working

**Symptoms:**
- Interceptor is called but `ClassCastException` still occurs

**Solutions:**
1. Check that filtering logic is correct
2. Verify `TurbineUserGroupRoleModelPeerMapper` interface is accessible
3. Check logs for filtering messages
4. Verify filtered collection is being passed to `proceed()`

---

## Files Modified

1. `webapp/pom.xml` - Added AspectJ dependency
2. `turbine-model-controller/src/main/java/com/uob/aspect/MaptoModelInterceptor.java` - Created aspect
3. `webapp/src/main/resources/META-INF/aop.xml` - Created LTW configuration
4. `spring-rest-api/src/main/java/com/uob/config/SpringConfig.java` - Added `@EnableAspectJAutoProxy`

---

## Next Steps

1. **Build the project:**
   ```bash
   mvn clean install -DskipTests -Ptomcat
   ```

2. **Configure Tomcat for LTW:**
   - Add `-javaagent` JVM argument
   - Restart Tomcat

3. **Deploy and test:**
   - Deploy WAR to Tomcat
   - Access application pages
   - Check logs for interceptor messages
   - Verify `ClassCastException` is resolved

---

## References

- [AspectJ Load-Time Weaving](https://www.eclipse.org/aspectj/doc/released/devguide/ltw.html)
- [AspectJ Documentation](https://www.eclipse.org/aspectj/doc/released/progguide/index.html)
- [Spring AOP Documentation](https://docs.spring.io/spring-framework/reference/core/aop.html)

# Architecture Challenges and Recommendations
## Legacy Application Migration: Jetspeed 1.x / Turbine 3.x / Torque 3.x → Modern Stack

**Date:** January 24, 2026  
**Project:** UOB Turbine 7 Portal Multi-Module  
**Context:** Critical Legacy Project Migration

---

## Executive Summary

This document explains why resolving issues in the legacy application migration is taking longer than expected, and provides recommendations for the multi-dimensional migration project.

### Current Legacy Stack
- **Application Server:** JBoss 7.4
- **Portal Framework:** Jetspeed 1.x
- **MVC Framework:** Turbine 3.x
- **View Layer:** Velocity Templates + Jetspeed PSML (Portal Structure Markup Language)
- **Portlet Architecture:** Jetspeed Portlets
- **ORM:** Torque 3.x
- **REST APIs:** Spring Framework 4.x
- **Data Processing:** XML/XSLT Transformation (Flat file → XML → XSLT → Subset extraction)
- **Runtime:** Java EE Container (Java 8)

### Target Modern Stack
- **Application Server:** JBoss 8 / WildFly
- **MVC Framework:** Turbine 7.x (interim) → Spring Boot (target)
- **View Layer:** Server-based or SPA-based (to preserve portlet layout UX)
- **ORM:** Torque 5.x (interim) → JPA/Hibernate (target)
- **REST APIs:** Spring Framework 6.x
- **Data Processing:** Modern XML/XSLT or alternative transformation approach
- **Runtime:** Jakarta EE Container (Java 17) - JBoss 8

### Migration Complexity
This is a **multi-dimensional migration** involving:
1. Application server migration (JBoss 7.4 → JBoss 8)
2. Framework version upgrades (Turbine 3.x → 7.x, Torque 3.x → 5.x, Spring 4.x → 6.x)
3. Java version upgrade (Java 8 → Java 17)
4. Java EE → Jakarta EE migration
5. Portal framework removal (Jetspeed 1.x → modern alternative)
6. View layer migration (Velocity + PSML → Server-based or SPA)
7. Portlet architecture migration (Jetspeed Portlets → modern alternative)
8. Data processing migration (XML/XSLT → modern approach)

**Key Finding:** The extended resolution time is primarily due to:
1. **Multi-Dimensional Migration Complexity** - Multiple simultaneous framework upgrades
2. **Legacy Architecture Complexity** - Deep inheritance hierarchies and framework coupling
3. **Limited Modern Documentation** - Frameworks are no longer actively maintained
4. **Type System Mismatches** - Framework assumptions about table naming and object relationships
5. **Java EE → Jakarta EE Migration** - Package namespace changes (javax.* → jakarta.*)
6. **Application Server Migration** - JBoss 7.4 → JBoss 8 with configuration and behavior changes
7. **View Layer Complexity** - Velocity templates + PSML portlet architecture
8. **Data Processing Complexity** - Complex XML/XSLT transformation pipelines
9. **User Experience Preservation** - Must maintain portlet layout and UX during migration
10. **Insufficient Debugging Tools** - Limited visibility into framework internals

---

## 1. Why Issues Take Longer to Resolve

### 1.0 Multi-Dimensional Migration Context

#### **Legacy Application Stack (Current Production)**
```
JBoss 7.4 (Application Server)
  └── Jetspeed 1.x (Portal Framework)
      └── PSML (Portal Structure Markup Language)
          └── Velocity Templates (View Layer)
              └── Jetspeed Portlets
                  └── Turbine 3.x (MVC Framework)
                      └── Torque 3.x (ORM)
                          └── XML/XSLT Processing
                              └── Java EE APIs (javax.*)
                                  └── Java 8 Runtime
```

#### **Interim Migration Stack (Current Project)**
```
JBoss 8 (Application Server) - Migrated from JBoss 7.4
  └── Turbine 7.x (MVC Framework) - Upgraded from 3.x
      └── Velocity Templates (View Layer) - Maintained
          └── Torque 5.x (ORM) - Upgraded from 3.x
              └── Spring Framework 6.x (REST APIs) - Upgraded from 4.x
                  └── XML/XSLT Processing - Maintained (to be modernized)
                      └── Jakarta EE APIs (jakarta.*) - Migrated from javax.*
                          └── Java 17 Runtime - Upgraded from Java 8
```

#### **Target Modern Stack (Future)**
```
JBoss 8 / WildFly (Application Server)
  └── Spring Boot 3.x (MVC Framework)
      └── Server-based View Layer OR SPA (View Layer)
          └── Portlet-like Layout (Preserved UX)
              └── Spring Security (Security Framework)
                  └── JPA/Hibernate (ORM)
                      └── Modern Data Processing (XML/XSLT alternative)
                          └── Jakarta EE APIs (jakarta.*)
                              └── Java 17+ Runtime
```

**Migration Complexity Multiplier:**
- Each dimension (framework, Java version, API namespace) multiplies complexity
- Framework upgrades introduce breaking changes
- Java 8 → 17 has significant language and API changes
- javax.* → jakarta.* namespace migration affects all dependencies
- Application server migration requires configuration changes

### 1.1 Legacy Architecture Characteristics

#### **Deep Inheritance Hierarchies**
```
GtpUser
  → BaseGtpUser (Torque-generated)
    → DefaultAbstractTurbineUser (Fulcrum Security)
      → TorqueAbstractSecurityEntity
        → [Multiple framework layers]
```

**Impact:**
- Method calls traverse 4-5 inheritance levels
- Override behavior is unpredictable across layers
- Framework methods may bypass our overrides
- Hard to trace which class actually executes

#### **Framework Coupling**
- Turbine 7 tightly couples with Fulcrum Security
- Fulcrum Security tightly couples with Torque ORM
- All three frameworks make assumptions about each other
- Changes in one layer affect others unpredictably

#### **Table Naming Assumptions**
The framework was designed for `TURBINE_*` tables:
- `TURBINE_USER`
- `TURBINE_GROUP`
- `TURBINE_ROLE`
- `TURBINE_USER_GROUP_ROLE`

Our project uses `GTP_*` tables:
- `GTP_USER`
- `GTP_GROUP`
- `GTP_ROLE`
- `GTP_USER_GROUP_ROLE`

**Result:** Framework code paths assume `TURBINE_*` table structure, causing:
- Incorrect join queries
- Type mismatches
- ClassCastExceptions

### 1.2 Framework Version Migration Challenges

#### **Turbine 3.x → 7.x Migration**
**Breaking Changes:**
- API changes between major versions
- Service initialization changes
- Configuration file format changes
- Dependency injection changes
- Velocity template engine updates

**Impact:**
- Existing Turbine 3.x code may not work in 7.x
- Configuration files need migration
- Custom services need refactoring
- Template syntax may need updates

#### **Torque 3.x → 5.x Migration**
**Breaking Changes:**
- Generated code structure changes
- Criteria API changes
- Connection handling changes
- Transaction management updates

**Impact:**
- Existing Torque 3.x OM classes incompatible
- Query code needs refactoring
- Connection pooling changes
- Transaction boundaries may need adjustment

#### **Spring 4.x → 6.x Migration**
**Breaking Changes:**
- Java 17+ requirement (Spring 6.x requires Java 17)
- Jakarta EE namespace (javax.* → jakarta.*)
- Deprecated API removal
- Configuration changes

**Impact:**
- All Spring code needs Jakarta EE namespace updates
- Java 8 code may not compile on Java 17
- Deprecated methods need replacement
- Configuration XML/annotations may need updates

### 1.3 Java 8 → Java 17 Migration Challenges

#### **Language Changes**
- Module system (JPMS)
- New language features (records, pattern matching, text blocks)
- Removed/deprecated APIs
- Security manager deprecation
- Reflection access restrictions

#### **API Changes**
- **javax.* → jakarta.* namespace migration**
  - `javax.servlet.*` → `jakarta.servlet.*`
  - `javax.persistence.*` → `jakarta.persistence.*`
  - `javax.annotation.*` → `jakarta.annotation.*`
  - Affects all Java EE/Jakarta EE dependencies

#### **Impact on Frameworks**
- All frameworks must support Jakarta EE
- Dependency versions must be Jakarta-compatible
- Application server must support Jakarta EE
- Configuration files may need updates

### 1.4 Limited Modern Documentation

#### **Framework Status**
- **Jetspeed 1.x:** End-of-life, no longer maintained
- **Turbine 3.x/7.x:** Last major release ~2015, minimal active development
- **Torque 3.x/5.x:** Legacy ORM, superseded by modern alternatives
- **Spring 4.x:** End-of-life (Spring 4.x EOL, must upgrade to 6.x for Java 17)

#### **Documentation Gaps**
- No migration guides from Turbine 3.x → 7.x
- No migration guides from Torque 3.x → 5.x
- Limited Java 8 → 17 migration guides for legacy frameworks
- No Jakarta EE migration guides for Turbine/Torque
- Outdated examples (Java 8 era, Java EE namespace)
- No migration guides for custom table naming
- Limited JBoss 8 deployment guides for Turbine applications

#### **Source Code as Documentation**
- Must read framework source code to understand behavior
- Framework code is complex and not well-commented
- No clear extension points documented
- Version differences not well-documented

### 1.5 Type System Mismatches

#### **The ClassCastException Problem**

**Root Cause:**
The framework's `retrieveAttachedObjects()` method:
1. Calls `getTurbineUserGroupRolesJoinTurbineGroup()`
2. Uses join queries that return composite results
3. Expects only `TurbineUserGroupRoleModelPeerMapper` objects
4. Receives `GtpGroup` objects from join queries
5. Attempts to cast `GtpGroup` → `TurbineUserGroupRoleModelPeerMapper`
6. **ClassCastException occurs**

**Why It's Hard to Fix:**
- Framework code is in compiled JARs (not modifiable)
- Must override methods at correct inheritance level
- Framework uses caching, making overrides ineffective
- Multiple code paths can trigger the same issue
- Framework internals are not well-documented

#### **Our Solution Attempts**

1. **Override join methods** - Use `doSelect()` instead of `doSelectJoin*()`
   - ✅ Prevents joined objects in results
   - ❌ Framework may still use cached results

2. **Override `retrieveAttachedObjects()`** - Filter results
   - ✅ Can intercept before `maptoModel()` is called
   - ❌ Still calls `super()`, which uses base class logic

3. **Override `maptoModel()`** - Filter before processing
   - ❌ Method doesn't exist in parent class (compilation error)

**Current Status:** Multiple overrides in place, but framework may still use cached results or different code paths.

### 1.6 Application Server Migration Challenges

#### **JBoss 7.4 → JBoss 8 Migration**
**Configuration Changes:**
- Deployment descriptors (web.xml, jboss-web.xml, jboss-deployment-structure.xml)
- Data source configuration (JNDI names, connection pools, datasource definitions)
- Security realm configuration (security domains, authentication)
- Class loading configuration (module dependencies, class loading isolation)
- Logging configuration (logging subsystem, log handlers)
- Subsystem configurations (transactions, messaging, clustering)

**JBoss 7.4 vs JBoss 8 Differences:**
- **JBoss 7.4:** Java EE 7, Java 8-11, WildFly 10-14 based
- **JBoss 8:** Jakarta EE 10, Java 17+, WildFly 26+ based
- **Namespace:** javax.* → jakarta.* (all Java EE APIs)
- **Module System:** Enhanced module system, stricter class loading
- **Configuration Format:** Some subsystem configurations changed
- **Deployment Model:** Enhanced deployment scanner, different hot deployment behavior

**Migration Challenges:**
- **Namespace Migration:** All javax.* references must change to jakarta.*
- **Module Dependencies:** Module names and dependencies may have changed
- **Configuration Compatibility:** Some JBoss 7.4 configurations may not work in JBoss 8
- **Class Loading:** Stricter class loading isolation may expose dependency issues
- **Performance:** Different performance characteristics, may need tuning

**Impact:**
- WAR file structure may need changes
- Configuration files need JBoss 8-specific updates
- Deployment process changes
- Runtime behavior may differ
- Testing required for all application features

### 1.7 Jetspeed 1.x Removal and View Layer Migration

#### **Portal Framework Removal**
**Jetspeed 1.x Functionality:**
- Portal page aggregation
- Portlet container
- PSML (Portal Structure Markup Language) for layout definition
- User personalization
- Portal administration
- Velocity template integration

#### **PSML (Portal Structure Markup Language)**
**PSML Characteristics:**
- XML-based layout definition
- Defines portal pages, portlets, and their arrangement
- Controls portlet positioning and sizing
- Manages portlet instances and configurations
- User-specific layout customization

**PSML Migration Challenges:**
- PSML files define complex portal layouts
- Portlet arrangements and relationships
- User personalization data
- Layout inheritance and customization
- Must be converted to modern layout system

#### **Velocity Templates**
**Velocity Template Usage:**
- View rendering for Turbine pages
- Portlet content rendering
- Layout templates
- Component templates
- Integration with PSML layouts

**Velocity Migration Considerations:**
- Velocity templates can be maintained in modern stack
- Spring Boot supports Velocity (with dependencies)
- Alternative: Migrate to Thymeleaf, FreeMarker, or JSP
- Must preserve template logic and functionality

#### **Portlet Architecture**
**Jetspeed Portlets:**
- Portlet API implementation (JSR 168/286)
- Portlet lifecycle management
- Portlet preferences and configuration
- Portlet communication (events, public render parameters)
- Portlet aggregation and layout

**Portlet Migration Options:**

**Option 1: Server-Based View Layer (Recommended for UX Preservation)**
- **Approach:** Maintain portlet-like layout using server-side rendering
- **Technologies:** Spring MVC + Thymeleaf/FreeMarker, Server-side components
- **Pros:**
  - Preserves portlet layout and UX
  - Familiar server-side development
  - SEO-friendly
  - Progressive enhancement possible
- **Cons:**
  - Full page reloads
  - Less interactive than SPA
- **Implementation:**
  - Convert PSML layouts to server-side layout templates
  - Create portlet-like components as server-side fragments
  - Use AJAX for partial updates
  - Maintain Velocity or migrate to Thymeleaf/FreeMarker

**Option 2: Single Page Application (SPA)**
- **Approach:** Modern SPA with portlet-like widget layout
- **Technologies:** React/Vue/Angular + Spring Boot REST APIs
- **Pros:**
  - Modern, interactive UX
  - Better performance (client-side rendering)
  - Rich user interactions
  - Component-based architecture
- **Cons:**
  - Significant development effort
  - May change UX (user training needed)
  - SEO challenges
  - More complex state management
- **Implementation:**
  - Convert PSML layouts to SPA component layouts
  - Create portlet-like widgets as SPA components
  - Use REST APIs for data
  - Implement client-side routing

**Option 3: Hybrid Approach**
- **Approach:** Server-side layout with SPA components
- **Technologies:** Spring MVC + SPA components (React/Vue micro-frontends)
- **Pros:**
  - Best of both worlds
  - Gradual migration possible
  - Preserves UX while modernizing
- **Cons:**
  - More complex architecture
  - Requires careful integration
- **Implementation:**
  - Server-side layout templates
  - SPA components for interactive portlets
  - REST APIs for SPA components
  - Progressive enhancement

**Recommended Approach: Server-Based View Layer (Option 1)**
- Minimizes UX disruption
- Faster migration timeline
- Lower risk
- Easier to maintain
- Can enhance with AJAX later

**Impact:**
- Portal-specific code must be removed or replaced
- PSML layouts must be converted to modern layout system
- Velocity templates must be maintained or migrated
- Portlet functionality must be reimplemented
- User personalization features need reimplementation
- Page aggregation logic needs redesign
- Administration features need rebuilding

### 1.8 XML/XSLT Data Processing Migration

#### **Current XML/XSLT Processing**
**Processing Pipeline:**
1. **Flat File Input** → Transform to XML
2. **XML Processing** → Apply XSLT transformations
3. **XSLT Transformation** → Extract subset of data
4. **Data Output** → Processed data for application use

**Complexity Factors:**
- Heavy use of XML/XSLT transformation
- Complex XSLT processing logic
- Multiple transformation stages
- Flat file to XML conversion
- Data subset extraction
- Performance-critical processing

#### **Migration Challenges**
**XSLT Processing:**
- XSLT 1.0/2.0 compatibility
- Complex XSLT templates
- XPath expressions
- Template matching
- Performance optimization
- Error handling

**XML Processing:**
- Large XML documents
- Memory management
- Streaming vs DOM processing
- Schema validation
- Transformation caching

#### **Migration Options**

**Option 1: Maintain XML/XSLT (Recommended for Stability)**
- **Approach:** Keep existing XML/XSLT processing
- **Technologies:** Java XML APIs, Saxon XSLT processor
- **Pros:**
  - Minimal changes to data processing logic
  - Proven, stable approach
  - Lower migration risk
  - Faster migration timeline
- **Cons:**
  - Legacy technology
  - Performance may not be optimal
  - Limited modern tooling
- **Implementation:**
  - Update to modern XML/XSLT libraries
  - Optimize XSLT templates
  - Improve error handling
  - Add monitoring and logging

**Option 2: Modern Data Processing**
- **Approach:** Replace XML/XSLT with modern alternatives
- **Technologies:** 
  - JSON + JSONPath/JSONTransform
  - Apache Camel for ETL
  - Spring Batch for batch processing
  - Stream processing (Kafka Streams, etc.)
- **Pros:**
  - Modern, maintainable approach
  - Better performance
  - Modern tooling and support
  - Easier to test and debug
- **Cons:**
  - Significant development effort
  - Must rewrite transformation logic
  - Higher migration risk
  - Requires comprehensive testing
- **Implementation:**
  - Analyze existing XSLT transformations
  - Design equivalent modern processing
  - Implement new processing pipeline
  - Migrate data formats (XML → JSON if applicable)
  - Comprehensive testing

**Option 3: Hybrid Approach**
- **Approach:** Keep XML/XSLT for critical paths, modernize others
- **Technologies:** Mix of XML/XSLT and modern processing
- **Pros:**
  - Gradual migration
  - Lower risk
  - Can optimize incrementally
- **Cons:**
  - Dual processing systems
  - More complex architecture
- **Implementation:**
  - Identify critical vs non-critical processing
  - Modernize non-critical paths first
  - Gradually migrate critical paths
  - Maintain compatibility during transition

**Recommended Approach: Maintain XML/XSLT Initially (Option 1)**
- Focus on framework/server migration first
- Modernize data processing in later phase
- Lower overall migration risk
- Faster initial migration timeline

**Impact:**
- Data processing logic must be analyzed
- XSLT templates must be documented
- Performance must be optimized
- Error handling must be improved
- May need modernization in future phase

### 1.9 Insufficient Debugging Tools

#### **Limited Visibility**
- Framework logs are verbose but not informative
- No clear indication which override is being called
- Stack traces show framework internals, not our code
- Hard to determine if overrides are actually executed

#### **Testing Challenges**
- Cannot easily unit test framework interactions
- Requires full application context (Turbine services)
- Database state affects behavior
- Caching makes behavior non-deterministic

---

## 2. Architecture Maturity Assessment

### 2.1 Framework Maturity

| Framework | Legacy Version | Current Version | Maturity | Active Development | Documentation | Community Support |
|-----------|----------------|-----------------|----------|-------------------|---------------|-------------------|
| Jetspeed | 1.x (EOL) | N/A | Legacy | ❌ None | ❌ None | ❌ None |
| Turbine | 3.x (Legacy) | 7.x | Legacy | ❌ Minimal | ⚠️ Limited | ⚠️ Small |
| Torque | 3.x (Legacy) | 5.x | Legacy | ❌ None | ⚠️ Limited | ❌ Minimal |
| Spring | 4.x (EOL) | 6.x | Mature | ✅ Active | ✅ Comprehensive | ✅ Large |
| Fulcrum Security | N/A | 4.x | Mature | ⚠️ Maintenance | ⚠️ Limited | ⚠️ Small |

**Verdict:** Frameworks are **mature but legacy** - stable but not actively improved.

### 2.2 Resource Availability

#### **Available Resources**
- ✅ Framework source code (Apache License)
- ✅ Basic API documentation
- ✅ Some Stack Overflow answers (dated)
- ✅ Maven Central artifacts
- ✅ Spring Framework 6.x documentation (comprehensive)
- ✅ Jakarta EE documentation
- ✅ Java 17 migration guides (general)

#### **Missing Resources**
- ❌ Turbine 3.x → 7.x migration guides
- ❌ Torque 3.x → 5.x migration guides
- ❌ Jetspeed 1.x removal/replacement guides
- ❌ Java EE → Jakarta EE migration guides for Turbine/Torque
- ❌ Best practices for custom table naming
- ❌ Troubleshooting guides for common issues
- ❌ Active community forums for legacy frameworks
- ❌ Professional support options for Turbine/Torque
- ❌ Modern IDE plugins/tooling for legacy frameworks
- ❌ JBoss 8 deployment guides for Turbine applications

### 2.3 Architecture Suitability for Modern Development

#### **Strengths**
- ✅ Proven stability (used in production for years)
- ✅ Comprehensive security framework
- ✅ Flexible ORM capabilities
- ✅ MVC architecture pattern

#### **Weaknesses**
- ❌ Tight coupling between components
- ❌ Complex inheritance hierarchies
- ❌ Limited extension points
- ❌ Hard to debug and troubleshoot
- ❌ No modern tooling support
- ❌ Difficult to unit test
- ❌ Performance may not match modern frameworks

---

## 3. Specific Challenges Encountered

### 3.0 Multi-Dimensional Migration Challenges

#### **Challenge 1: Framework Version Compatibility**
- Turbine 3.x code may not work in 7.x
- Torque 3.x OM classes incompatible with 5.x
- Spring 4.x dependencies may conflict with 6.x
- All frameworks must support Jakarta EE

#### **Challenge 2: Java Version Compatibility**
- Java 8 code may use deprecated APIs removed in Java 17
- Reflection access restrictions in Java 17
- Module system (JPMS) may affect class loading
- Security manager deprecation affects security code

#### **Challenge 3: Namespace Migration**
- All `javax.*` imports must change to `jakarta.*`
- Affects servlet APIs, JPA, annotations, etc.
- Dependency versions must be Jakarta-compatible
- Configuration files may reference old namespaces

#### **Challenge 4: Application Server Migration**
- Deployment descriptors need JBoss 8 format
- Data source configuration changes
- Class loading behavior differs
- Logging configuration changes

### 3.1 ClassCastException Resolution Timeline

**Issue:** `GtpGroup cannot be cast to TurbineUserGroupRoleModelPeerMapper`

**Attempts Made:**
1. ✅ Override `getGtpUserGroupRolesJoinGtpGroup()` - Use `doSelect()` instead of join
2. ✅ Override `getGtpUserGroupRolesJoinGtpRole()` - Use `doSelect()` instead of join
3. ✅ Override `getTurbineUserGroupRolesJoinTurbineGroup()` - Use `doSelect()` instead of join
4. ✅ Override `getTurbineUserGroupRolesJoinTurbineRole()` - Use `doSelect()` instead of join
5. ✅ Override `retrieveAttachedObjects()` - Call super() to use overridden methods
6. ❌ Attempt to override `maptoModel()` - Method doesn't exist in parent

**Why It's Taking So Long:**
- Each attempt requires rebuild, redeploy, and testing
- Framework caching makes it hard to verify if overrides are working
- Multiple code paths can trigger the same issue
- Framework internals are not well-documented
- Must read source code to understand behavior

### 3.2 Framework Upgrade Challenges

#### **Turbine 3.x → 7.x Issues**
- Service initialization changes
- Configuration file format differences
- API method signature changes
- Dependency injection mechanism changes
- Velocity template engine updates

#### **Torque 3.x → 5.x Issues**
- Generated code structure completely different
- Criteria API changes (method names, parameters)
- Connection handling changes
- Transaction management updates
- OM class inheritance changes

#### **Spring 4.x → 6.x Issues**
- Java 17 requirement (cannot use Java 8)
- Jakarta EE namespace migration required
- Deprecated API removal
- Configuration changes (XML/annotations)
- Dependency version conflicts

### 3.3 Java 8 → Java 17 Migration Issues

#### **Compilation Errors**
- Deprecated API usage
- Reflection access restrictions
- Module system restrictions
- Removed APIs (e.g., SecurityManager)

#### **Runtime Issues**
- Class loading behavior changes
- Reflection behavior changes
- Security restrictions
- Performance characteristics differ

### 3.4 Jakarta EE Migration Issues

#### **Package Namespace Changes**
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.annotation.*` → `jakarta.annotation.*`
- `javax.validation.*` → `jakarta.validation.*`

#### **Impact**
- All imports must be updated
- Configuration files may reference old packages
- Dependency versions must be Jakarta-compatible
- Application server must support Jakarta EE

### 3.5 JBoss 7.4 → JBoss 8 Migration Issues

#### **Configuration Migration**
- Deployment descriptors need updates
- Module dependencies may have changed
- Subsystem configurations may differ
- Class loading behavior changes
- Security realm configuration updates

#### **Runtime Behavior Differences**
- Class loading isolation stricter
- Performance characteristics differ
- Memory management changes
- Threading model differences
- Transaction behavior may differ

#### **Testing Requirements**
- Comprehensive functional testing
- Performance testing
- Load testing
- Security testing
- Compatibility testing

### 3.6 View Layer Migration Issues

#### **PSML to Modern Layout**
- PSML layouts must be converted
- Portlet arrangements must be preserved
- User personalization must be maintained
- Layout inheritance must be handled
- Responsive design considerations

#### **Velocity Template Migration**
- Template syntax compatibility
- Variable and method references
- Macro definitions
- Template inheritance
- Integration with new layout system

#### **Portlet to Component Migration**
- Portlet lifecycle to component lifecycle
- Portlet preferences to component configuration
- Portlet communication to component communication
- Portlet aggregation to component layout

### 3.7 XML/XSLT Processing Migration Issues

#### **XSLT Compatibility**
- XSLT version differences
- XPath expression compatibility
- Template matching behavior
- Extension functions
- Performance optimization

#### **XML Processing**
- Large document handling
- Memory management
- Streaming vs DOM
- Schema validation
- Error handling

### 3.8 Build System Complexity

**Multi-Module Maven Project:**
- `torque-orm` - Torque ORM generation
- `turbine-model-controller` - Turbine MVC
- `spring-rest-api` - Spring Framework integration
- `webapp` - WAR file assembly

**Challenges:**
- Generated base classes must be committed to version control
- Build order matters (Torque generation before compilation)
- Profile-specific WAR files (Tomcat vs JBoss)
- Complex dependency management

### 3.9 Database Schema Mismatch

**Framework Expects:**
- `TURBINE_USER`, `TURBINE_GROUP`, `TURBINE_ROLE`, etc.

**Project Uses:**
- `GTP_USER`, `GTP_GROUP`, `GTP_ROLE`, etc.

**Impact:**
- Must override every method that references table names
- Framework code paths assume `TURBINE_*` naming
- Join queries may use wrong table names
- Type mappings may be incorrect

---

## 4. Recommendations for Legacy Migration Project

### 4.0 Migration Strategy Overview

#### **Phase 1: Framework Upgrades (Current)**
- ✅ Turbine 3.x → 7.x
- ✅ Torque 3.x → 5.x
- ✅ Spring 4.x → 6.x
- ✅ Java 8 → Java 17
- ✅ Java EE → Jakarta EE
- ⏳ JBoss 8 deployment
- ❌ Jetspeed 1.x removal (future)

#### **Phase 2: Stabilization (Next 3-6 Months)**
- Complete all framework upgrades
- Resolve compatibility issues
- Comprehensive testing
- Performance optimization
- Documentation

#### **Phase 3: Modern Stack Migration (6-18 Months)**
- Turbine 7.x → Spring Boot
- Torque 5.x → JPA/Hibernate
- Complete Jetspeed removal
- Full Jakarta EE adoption

### 4.1 Short-Term Recommendations (Current Project)

#### **Immediate Actions**
1. **Complete Current Fixes**
   - Ensure all join method overrides are in place
   - Rebuild and redeploy with latest changes
   - Test thoroughly to verify `ClassCastException` is resolved
   - Verify Jakarta EE namespace migration is complete
   - Test on Java 17 runtime

2. **Add Comprehensive Logging**
   - Log when overridden methods are called
   - Log framework method calls
   - Track which code paths are executed
   - Log Jakarta EE namespace usage
   - This will help identify if overrides are working

3. **Create Test Cases**
   - Unit tests for overridden methods
   - Integration tests for user/group/role retrieval
   - Verify no `ClassCastException` occurs
   - Test Java 17 compatibility
   - Test Jakarta EE namespace usage

4. **JBoss 8 Deployment Testing**
   - Test WAR deployment on JBoss 8
   - Verify data source configuration
   - Test class loading behavior
   - Verify logging configuration
   - Performance testing

#### **Risk Mitigation**
1. **Document All Overrides**
   - List every method that was overridden
   - Document why each override was necessary
   - Note any framework assumptions that were violated
   - Document framework version differences
   - Document Java 8 → 17 changes
   - Document Jakarta EE namespace changes

2. **Create Troubleshooting Guide**
   - Document common issues and solutions
   - List all override locations
   - Provide debugging steps
   - Document framework upgrade issues
   - Document Java version compatibility issues
   - Document Jakarta EE migration issues

3. **Migration Documentation**
   - Document Turbine 3.x → 7.x changes
   - Document Torque 3.x → 5.x changes
   - Document Spring 4.x → 6.x changes
   - Document Java 8 → 17 changes
   - Document Java EE → Jakarta EE changes
   - Document JBoss 8 deployment steps

### 4.2 Medium-Term Recommendations (Next 6 Months)

#### **Stabilization Phase**
1. **Complete Framework Upgrades**
   - Finalize Turbine 7.x migration
   - Finalize Torque 5.x migration
   - Finalize Spring 6.x migration
   - Complete Java 17 migration
   - Complete Jakarta EE migration
   - Complete JBoss 8 deployment

2. **Jetspeed Removal Planning**
   - Identify Jetspeed-dependent features
   - Plan replacement strategy
   - Estimate effort for each feature
   - Prioritize features for migration

3. **Performance Analysis**
   - Profile application performance on Java 17
   - Compare with Java 8 performance
   - Identify bottlenecks
   - Optimize critical paths
   - Compare with modern framework alternatives

#### **Architecture Assessment**
1. **Evaluate Final Migration Path**
   - Assess effort to migrate Turbine → Spring Boot
   - Assess effort to migrate Torque → JPA/Hibernate
   - Evaluate Jetspeed replacement options
   - Estimate complete migration timeline and cost

2. **Maintenance Plan**
   - Identify critical dependencies
   - Plan for security updates
   - Document knowledge transfer requirements
   - Plan for ongoing framework support

#### **Knowledge Management**
1. **Create Internal Documentation**
   - Architecture diagrams
   - Customization guide
   - Troubleshooting playbook
   - Team training materials

2. **Establish Support Process**
   - Define escalation paths
   - Create runbooks for common issues
   - Set up monitoring and alerting

### 4.3 Long-Term Recommendations (1-2 Years)

#### **Complete Modern Stack Migration**

**Target Architecture:**
```
Spring Boot 3.x (MVC Framework)
  └── Spring Security (Security Framework)
      └── JPA/Hibernate (ORM)
          └── Jakarta EE APIs (jakarta.*)
              └── Java 17+ Runtime
                  └── JBoss 8 / WildFly
```

#### **Migration Strategy Options**

**Option 1: Incremental Migration**
- Keep Turbine 7.x for existing web UI features
- Use Spring Boot for new features
- Gradually migrate modules from Turbine to Spring Boot
- **Pros:** Lower risk, gradual transition, can maintain production stability
- **Cons:** Dual framework maintenance, longer timeline

**Option 2: Complete Migration**
- Migrate entire application to Spring Boot
- Replace Turbine 7.x with Spring MVC
- Replace Fulcrum Security with Spring Security
- Replace Torque 5.x with JPA/Hibernate
- Remove all Jetspeed dependencies
- **Pros:** Modern stack, better tooling, single framework
- **Cons:** High effort, higher risk, requires comprehensive testing

**Option 3: Hybrid Approach (Recommended)**
- Keep Turbine 7.x for core web UI features (temporary)
- Use Spring Boot for REST APIs ✅ (Already done)
- Gradually migrate Turbine features to Spring Boot
- Remove Jetspeed features incrementally
- **Pros:** Leverage existing Spring integration, lower risk, manageable timeline
- **Cons:** Still maintain Turbine components during transition

#### **Recommended Approach: Hybrid (Option 3) - Phased Migration**

**Phase 1: Framework Upgrades (Current - 0-6 months)** ✅ In Progress
- JBoss 7.4 → JBoss 8 migration
- Turbine 3.x → 7.x
- Torque 3.x → 5.x
- Spring 4.x → 6.x
- Java 8 → Java 17
- Java EE → Jakarta EE
- Deploy to JBoss 8
- Maintain Velocity templates
- Maintain XML/XSLT processing

**Phase 2: Stabilization (3-6 months)**
- Complete all framework upgrades
- Resolve compatibility issues
- Comprehensive testing
- Performance optimization
- Documentation
- JBoss 8 deployment validation

**Phase 3: View Layer Migration (6-18 months)**
- Analyze PSML layouts and portlet architecture
- Design modern layout system (server-based recommended)
- Convert PSML layouts to modern layout templates
- Migrate Velocity templates (maintain or migrate to Thymeleaf/FreeMarker)
- Implement portlet-like components as server-side fragments
- Preserve user personalization features
- Test UX preservation
- Remove Jetspeed dependencies
- Deploy and validate

**Phase 4: Turbine → Spring Boot Migration (12-18 months)**
- Migrate Turbine security to Spring Security
- Migrate Turbine MVC to Spring MVC
- Migrate Turbine services to Spring services
- Remove Turbine dependencies
- Integrate with new view layer

**Phase 5: Torque → JPA Migration (6-12 months)**
- Replace Torque with JPA/Hibernate
- Migrate OM classes to JPA entities
- Update all database queries
- Remove Torque dependencies

**Phase 6: Data Processing Modernization (6-12 months) - Optional**
- Analyze XML/XSLT processing requirements
- Evaluate modern alternatives
- Modernize non-critical processing paths
- Optimize critical processing paths
- Consider JSON/JSONPath or Apache Camel
- Improve performance and maintainability

**Phase 7: Final Modernization (3-6 months)**
- Complete Spring Boot migration
- Optimize for modern stack
- Full Jakarta EE adoption
- Complete view layer migration
- Production deployment
- Performance optimization

---

## 5. Technical Debt Assessment

### 5.1 Current Technical Debt

| Area | Debt Level | Impact | Effort to Fix |
|------|------------|--------|---------------|
| Legacy Framework Stack | Very High | Very High | Very High |
| Framework Coupling | High | High | Very High |
| Multi-Version Migration | High | High | Very High |
| Custom Table Naming | Medium | Medium | High |
| Override Complexity | Medium | Medium | Medium |
| Java Version Migration | Medium | Medium | Medium |
| Jakarta EE Migration | Medium | Medium | Medium |
| Jetspeed Dependency | High | Medium | High |
| Documentation | High | Medium | Medium |
| Testing Coverage | High | High | High |

### 5.2 Debt Accumulation Rate

**Factors Increasing Debt:**
- Multiple legacy frameworks (Jetspeed, Turbine 3.x, Torque 3.x)
- Framework versions no longer actively maintained
- Limited community support
- New developers need extensive training
- Each customization adds complexity
- Hard to refactor due to tight coupling
- Multi-dimensional migration complexity
- Java 8 → 17 migration adds complexity
- Jakarta EE migration adds complexity

**Debt Accumulation:**
- **Current:** Very High (legacy stack + migration in progress)
- **After Phase 1:** High (Turbine 7.x, Torque 5.x, but still legacy)
- **After Phase 4:** Medium (Spring Boot, but still Torque)
- **After Phase 5:** Low (Modern stack)

**Recommendation:** 
- **Short-term (0-6 months):** Complete Phase 1, stabilize
- **Medium-term (6-18 months):** Execute Phases 2-3, reduce debt
- **Long-term (18-36 months):** Complete Phases 4-5, achieve modern stack
- **Critical:** Do not delay migration beyond 3 years to avoid unsustainable debt

---

## 6. Resource Requirements

### 6.1 Current Project Resources

**Required Skills:**
- Deep understanding of Turbine 3.x/7.x architecture
- Deep understanding of Torque 3.x/5.x architecture
- Spring Framework 4.x/6.x expertise
- Java 8 → 17 migration experience
- Jakarta EE migration experience
- JBoss 8 deployment expertise
- Java reflection and inheritance expertise
- Database schema design
- Maven multi-module projects
- Legacy framework troubleshooting
- Jetspeed 1.x knowledge (for removal planning)

**Time Investment:**
- **Phase 1 (Framework Upgrades):** 3-6 months
  - Turbine 3.x → 7.x: 4-6 weeks
  - Torque 3.x → 5.x: 4-6 weeks
  - Spring 4.x → 6.x: 2-3 weeks
  - Java 8 → 17: 2-3 weeks
  - Jakarta EE migration: 2-3 weeks
  - JBoss 8 deployment: 2-3 weeks
  - Integration testing: 2-3 weeks
- **Issue resolution:** 1-2 weeks per major issue
- **Documentation:** Ongoing
- **Knowledge transfer:** 2-3 weeks per developer

### 6.2 Migration Project Resources

#### **Phase 1: Framework Upgrades (Current)**
- **Team Size:** 2-3 developers
- **Duration:** 3-6 months
- **Skills Required:**
  - Turbine 3.x/7.x expertise
  - Torque 3.x/5.x expertise
  - Spring 4.x/6.x expertise
  - Java 8 → 17 migration experience
  - Jakarta EE migration experience
  - JBoss 8 deployment experience

#### **Phase 2: Stabilization**
- **Team Size:** 2-3 developers
- **Duration:** 3-6 months
- **Skills Required:**
  - Same as Phase 1
  - Performance tuning
  - Testing expertise

#### **Phase 3: Jetspeed Removal**
- **Team Size:** 2-3 developers
- **Duration:** 6-12 months
- **Skills Required:**
  - Jetspeed 1.x knowledge
  - Portal framework alternatives
  - UI/UX design
  - Frontend development

#### **Phase 4: Turbine → Spring Boot Migration**
- **Team Size:** 3-4 developers
- **Duration:** 12-18 months
- **Skills Required:**
  - Spring Boot expertise
  - Spring MVC expertise
  - Spring Security expertise
  - Turbine 7.x knowledge (for migration)

#### **Phase 5: Torque → JPA Migration**
- **Team Size:** 2-3 developers
- **Duration:** 6-12 months
- **Skills Required:**
  - JPA/Hibernate expertise
  - Database design
  - Query optimization
  - Torque 5.x knowledge (for migration)

#### **If Staying with Current Stack (Not Recommended):**
- Legacy framework expertise: 1-2 developers (very hard to find)
- Maintenance effort: Ongoing, increasing over time
- Risk: Very high - frameworks becoming obsolete
- Cost: Increasing support and maintenance costs
- Timeline: Unsustainable beyond 2-3 years

---

## 7. Conclusion

### 7.1 Why Issues Take Longer

1. **Multi-Dimensional Migration:** Simultaneous upgrades across 5+ dimensions (frameworks, Java version, API namespace, application server)
2. **Architecture Complexity:** Deep inheritance, tight coupling, framework assumptions
3. **Legacy Framework Stack:** Jetspeed 1.x, Turbine 3.x, Torque 3.x - all end-of-life or legacy
4. **Version Migration Complexity:** Major version upgrades (3.x → 7.x, 3.x → 5.x, 4.x → 6.x) with breaking changes
5. **Java Version Migration:** Java 8 → 17 with language and API changes
6. **Jakarta EE Migration:** Complete namespace change (javax.* → jakarta.*) affecting all dependencies
7. **Limited Resources:** Outdated documentation, small community, no modern tooling
8. **Type System Mismatches:** Framework designed for `TURBINE_*` tables, we use `GTP_*`
9. **Debugging Challenges:** Hard to trace execution, caching issues, multiple code paths
10. **Application Server Migration:** JBoss 8 deployment with different configuration and behavior

### 7.2 Is the Architecture Mature?

**Legacy Stack Assessment:**

**Jetspeed 1.x:**
- ❌ End-of-life, no longer maintained
- ❌ No modern alternatives or migration paths
- ❌ Must be completely removed/replaced

**Turbine 3.x/7.x:**
- ⚠️ Stable but legacy (3.x is very old, 7.x is newer but still legacy)
- ✅ Comprehensive feature set
- ❌ No longer actively developed
- ❌ Limited modern resources and support
- ❌ Difficult to extend and maintain

**Torque 3.x/5.x:**
- ⚠️ Stable but legacy (3.x is very old, 5.x is newer but still legacy)
- ✅ ORM functionality works
- ❌ No longer actively developed
- ❌ Limited modern resources and support
- ❌ Difficult to extend and maintain

**Spring 4.x/6.x:**
- ✅ 4.x is end-of-life but 6.x is actively maintained
- ✅ Comprehensive feature set
- ✅ Excellent documentation and support
- ✅ Modern tooling and community

**Overall Assessment:**
- **Legacy Components:** Jetspeed, Turbine, Torque - all legacy/end-of-life
- **Modern Components:** Spring 6.x - actively maintained
- **Migration Status:** In progress, but legacy components remain
- **Recommendation:** Complete migration to modern stack within 2-3 years

### 7.3 Recommendations

**For Current Project (Phase 1):**
1. Complete current fixes with comprehensive testing
2. Complete Java 8 → 17 migration
3. Complete Jakarta EE namespace migration
4. Complete JBoss 8 deployment
5. Add extensive logging and monitoring
6. Document all customizations and overrides
7. Create troubleshooting guides
8. Document all framework version differences

**For Legacy Migration:**
1. **Short-term (0-6 months):** Complete Phase 1, stabilize framework upgrades
2. **Medium-term (6-18 months):** Execute Phases 2-3 (stabilization, Jetspeed removal)
3. **Long-term (18-36 months):** Execute Phases 4-5 (Turbine → Spring Boot, Torque → JPA)

**Recommended Migration Path:**
- **Phase 1 (Current):** Framework upgrades to interim stack ✅ In Progress
- **Phase 2-3:** Stabilization and Jetspeed removal
- **Phase 4:** Turbine 7.x → Spring Boot migration
- **Phase 5:** Torque 5.x → JPA/Hibernate migration
- **Target:** Complete modern stack (Spring Boot + Spring Security + JPA) within 2-3 years

**Critical Success Factors:**
- Maintain production stability during migration
- Comprehensive testing at each phase
- Knowledge transfer and documentation
- Resource allocation (skilled developers)
- Timeline adherence (do not delay beyond 3 years)

### 7.4 Critical Success Factors

1. **Knowledge Management:** 
   - Document everything (framework versions, changes, overrides)
   - Train team members on legacy and modern stacks
   - Create runbooks for common issues
   - Maintain architecture decision records (ADRs)

2. **Risk Mitigation:** 
   - Plan for framework limitations
   - Have backup strategies for each phase
   - Maintain rollback plans
   - Test thoroughly before production deployment

3. **Migration Planning:** 
   - Start planning now, execute gradually
   - Phase the migration to reduce risk
   - Maintain production stability throughout
   - Set realistic timelines

4. **Resource Allocation:** 
   - Ensure skilled developers are available
   - Plan for knowledge transfer
   - Consider external expertise if needed
   - Budget for training and documentation

5. **Monitoring:** 
   - Track issues, performance, and technical debt
   - Monitor framework compatibility
   - Track migration progress
   - Measure success metrics

6. **Java/Jakarta EE Migration:**
   - Complete namespace migration (javax.* → jakarta.*)
   - Test thoroughly on Java 17
   - Verify JBoss 8 compatibility
   - Document all changes

7. **Application Server Migration:**
   - Test deployment on JBoss 8
   - Verify configuration compatibility
   - Test performance and behavior
   - Document deployment procedures

---

## 8. Appendix: Framework Comparison

### 8.1 Legacy Stack vs Modern Stack

#### **Portal Framework**

| Aspect | Jetspeed 1.x | Modern Alternatives |
|--------|-------------|---------------------|
| **Status** | ❌ End-of-life | ✅ Active |
| **Age** | ~20 years | Modern |
| **Active Development** | ❌ None | ✅ Active |
| **Documentation** | ❌ None | ✅ Comprehensive |
| **Community** | ❌ None | ✅ Large |
| **Recommendation** | **Remove/Replace** | Liferay, GateIn, or custom |

#### **MVC Framework**

| Aspect | Turbine 3.x/7.x | Spring Boot |
|--------|-----------------|-------------|
| **Age** | ~20 years | ~10 years |
| **Active Development** | ❌ Minimal | ✅ Active |
| **Documentation** | ⚠️ Limited | ✅ Comprehensive |
| **Community** | ⚠️ Small | ✅ Large |
| **Tooling** | ⚠️ Limited | ✅ Excellent |
| **Testing** | ⚠️ Difficult | ✅ Easy |
| **Performance** | ⚠️ Good | ✅ Excellent |
| **Learning Curve** | ⚠️ Steep | ✅ Moderate |
| **Java 17 Support** | ⚠️ Limited | ✅ Full |
| **Jakarta EE Support** | ⚠️ Limited | ✅ Full |

#### **ORM Framework**

| Aspect | Torque 3.x/5.x | JPA/Hibernate |
|--------|----------------|---------------|
| **Age** | ~20 years | ~15 years |
| **Active Development** | ❌ None | ✅ Active |
| **Documentation** | ⚠️ Limited | ✅ Comprehensive |
| **Community** | ❌ Minimal | ✅ Large |
| **Tooling** | ⚠️ Limited | ✅ Excellent |
| **Testing** | ⚠️ Difficult | ✅ Easy |
| **Performance** | ⚠️ Good | ✅ Excellent |
| **Learning Curve** | ⚠️ Steep | ✅ Moderate |
| **Java 17 Support** | ⚠️ Limited | ✅ Full |
| **Jakarta EE Support** | ⚠️ Limited | ✅ Full |

#### **REST API Framework**

| Aspect | Spring 4.x | Spring 6.x |
|--------|------------|------------|
| **Status** | ❌ End-of-life | ✅ Active |
| **Java Support** | Java 8+ | Java 17+ |
| **Jakarta EE** | ❌ No | ✅ Yes |
| **Active Development** | ❌ None | ✅ Active |
| **Documentation** | ⚠️ Limited | ✅ Comprehensive |
| **Community** | ⚠️ Small | ✅ Large |
| **Recommendation** | **Must Upgrade** | ✅ Current |

### 8.2 Runtime Environment Comparison

#### **Java Version**

| Aspect | Java 8 | Java 17 |
|--------|--------|---------|
| **Status** | ⚠️ Legacy | ✅ LTS (Current) |
| **Support** | ⚠️ Limited | ✅ Full |
| **Security** | ⚠️ Older | ✅ Latest |
| **Performance** | ⚠️ Good | ✅ Better |
| **Language Features** | Limited | Modern |
| **Module System** | ❌ No | ✅ Yes (JPMS) |
| **Recommendation** | **Must Upgrade** | ✅ Target |

#### **API Namespace**

| Aspect | Java EE (javax.*) | Jakarta EE (jakarta.*) |
|--------|------------------|------------------------|
| **Status** | ❌ Deprecated | ✅ Current |
| **Support** | ❌ None | ✅ Full |
| **Compatibility** | Legacy servers | Modern servers |
| **Future** | ❌ No future | ✅ Active development |
| **Recommendation** | **Must Migrate** | ✅ Target |

#### **Application Server**

| Aspect | Legacy Server | JBoss 8 / WildFly |
|--------|--------------|-------------------|
| **Java Support** | Java 8 | Java 17+ |
| **Jakarta EE** | ❌ No | ✅ Yes (Jakarta EE 10) |
| **Active Development** | ❌ None | ✅ Active |
| **Documentation** | ⚠️ Limited | ✅ Comprehensive |
| **Community** | ⚠️ Small | ✅ Large |
| **Recommendation** | **Must Migrate** | ✅ Target |

---

## 9. Migration Timeline Summary

### 9.1 Current Status (Phase 1)

**Completed:**
- ✅ Multi-module Maven project structure
- ✅ Turbine 7.x integration
- ✅ Torque 5.x integration
- ✅ Spring Framework 6.x integration
- ✅ Jakarta EE namespace migration (in progress)
- ✅ Java 17 compatibility (in progress)
- ✅ JBoss 8 deployment configuration (in progress)

**In Progress:**
- ⏳ ClassCastException resolution
- ⏳ Complete Jakarta EE namespace migration
- ⏳ Complete Java 17 testing
- ⏳ JBoss 8 deployment testing

**Pending:**
- ❌ Jetspeed 1.x removal
- ❌ Complete modern stack migration

### 9.2 Estimated Timeline

| Phase | Duration | Start | End | Status |
|-------|----------|-------|-----|--------|
| Phase 1: Framework Upgrades | 3-6 months | Q1 2026 | Q2-Q3 2026 | ⏳ In Progress |
| Phase 2: Stabilization | 3-6 months | Q3 2026 | Q4 2026-Q1 2027 | 📅 Planned |
| Phase 3: View Layer Migration | 6-18 months | Q1 2027 | Q2 2027-Q2 2028 | 📅 Planned |
| Phase 4: Turbine → Spring Boot | 12-18 months | Q2 2028 | Q2 2028-Q2 2029 | 📅 Planned |
| Phase 5: Torque → JPA | 6-12 months | Q2 2029 | Q2 2029-Q2 2020 | 📅 Planned |
| Phase 6: Data Processing Modernization | 6-12 months | Q2 2029 | Q2 2029-Q2 2030 | 📅 Optional |
| Phase 7: Final Modernization | 3-6 months | Q2 2030 | Q2 2030-Q4 2030 | 📅 Planned |
| **Total Migration** | **36-66 months** | **Q1 2026** | **Q4 2030** | **📅 Planned** |

### 9.3 Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Framework compatibility issues | High | High | Comprehensive testing, phased approach |
| Java 17 migration issues | Medium | Medium | Gradual migration, thorough testing |
| Jakarta EE migration issues | Medium | Medium | Automated migration tools, testing |
| JBoss 8 deployment issues | Medium | Medium | Early testing, configuration documentation |
| Resource availability | Medium | High | Plan ahead, consider external expertise |
| Timeline delays | High | Medium | Buffer time, phased approach |
| Production stability | Medium | Very High | Comprehensive testing, rollback plans |

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-24 | AI Assistant | Initial document creation |
| 2.0 | 2026-01-24 | AI Assistant | Updated with legacy stack details (Jetspeed 1.x, Turbine 3.x, Torque 3.x, Spring 4.x, Java 8, Java EE) and target stack (Java 17, Jakarta EE, JBoss 8) |

---

**Note:** This document should be reviewed and updated as the project progresses and migration plans are finalized.

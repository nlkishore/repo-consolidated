# Security Audit Report: uob-turbine7-portal-mm

**Project**: UOB Turbine 7 Portal Multi-Module  
**Audit Date**: 2026-01-24  
**Auditor**: Automated Security Review  
**Status**: ⚠️ Requires Manual Verification

---

## Executive Summary

This document provides a comprehensive security audit of all libraries and dependencies used in the `uob-turbine7-portal-mm` project, including:

1. **Dependency Inventory**: Complete list of all libraries and versions
2. **CVE Assessment**: Known security vulnerabilities
3. **Customized Classes**: Open source library classes that have been customized
4. **Security Recommendations**: Action items for remediation

---

## 1. Dependency Inventory

### 1.1 Core Framework Dependencies

| Library | Group ID | Artifact ID | Version | Status |
|---------|----------|-------------|---------|--------|
| **Apache Turbine** | org.apache.turbine | turbine | 7.0 | ✅ Current |
| **Apache Torque** | org.apache.torque | torque-runtime | 6.0 | ✅ Current |
| **Spring Framework** | org.springframework | spring-context | 6.1.5 | ✅ Current |
| **Spring Web MVC** | org.springframework | spring-webmvc | 6.1.5 | ✅ Current |
| **Jackson** | com.fasterxml.jackson.core | jackson-databind | 2.17.1 | ⚠️ Check for updates |
| **Log4j2** | org.apache.logging.log4j | log4j-api | 2.23.1 | ✅ Recent (post-CVE) |
| **Log4j2 Web** | org.apache.logging.log4j | log4j-jakarta-web | 2.23.1 | ✅ Recent |
| **MySQL Connector** | com.mysql | mysql-connector-j | 9.1.0 | ✅ Current |

### 1.2 Fulcrum Dependencies

| Library | Group ID | Artifact ID | Version | Status |
|---------|----------|-------------|---------|--------|
| **Fulcrum YAAFI** | org.apache.fulcrum | fulcrum-yaafi | 2.0.1 | ✅ Current |
| **Fulcrum Security API** | org.apache.fulcrum | fulcrum-security-api | 4.0.0 | ✅ Current |
| **Fulcrum Security Torque** | org.apache.fulcrum | fulcrum-security-torque | 4.0.0 | ✅ Current |
| **Fulcrum Security Memory** | org.apache.fulcrum | fulcrum-security-memory | 4.0.0 | ✅ Current |
| **Fulcrum Intake** | org.apache.fulcrum | fulcrum-intake | 4.0.0 | ✅ Current |
| **Fulcrum Cache** | org.apache.fulcrum | fulcrum-cache | 2.0.1 | ✅ Current |
| **Fulcrum JSON** | org.apache.fulcrum | fulcrum-json-jackson2 | 2.0.1 | ✅ Current |

### 1.3 Testing Dependencies

| Library | Group ID | Artifact ID | Version | Status |
|---------|----------|-------------|---------|--------|
| **JUnit Jupiter** | org.junit.jupiter | junit-jupiter | 5.10.2 | ✅ Current |
| **Spring Test** | org.springframework | spring-test | 6.1.5 | ✅ Current |

### 1.4 API Documentation

| Library | Group ID | Artifact ID | Version | Status |
|---------|----------|-------------|---------|--------|
| **SpringDoc OpenAPI** | org.springdoc | springdoc-openapi-starter-webmvc-ui | 2.3.0 | ✅ Current |

### 1.5 Servlet API

| Library | Group ID | Artifact ID | Version | Status |
|---------|----------|-------------|---------|--------|
| **Jakarta Servlet API** | jakarta.servlet | jakarta.servlet-api | 6.1.0 | ✅ Current |
| **JBoss Servlet API** | org.jboss.spec.javax.servlet | jboss-servlet-api_4.0_spec | 2.0.0.Final | ✅ Current (JBoss profile) |

---

## 2. Known Security Vulnerabilities (CVE Assessment)

### ⚠️ CRITICAL: Manual Verification Required

**This audit requires running automated CVE scanning tools** to identify all vulnerabilities. The following are known concerns based on library versions:

### 2.1 Log4j2 (CVE-2021-44228, CVE-2021-45046)

**Status**: ✅ **SAFE** - Version 2.23.1 is **post-vulnerability**  
- **CVE-2021-44228** (Log4Shell): Fixed in 2.15.0+
- **CVE-2021-45046**: Fixed in 2.16.0+
- **Current Version**: 2.23.1 ✅

**Action**: No action required - version is secure.

### 2.2 Jackson (CVE-2020-25649, CVE-2020-36518)

**Status**: ⚠️ **VERIFY** - Version 2.17.1 may have vulnerabilities  
- **CVE-2020-25649**: Polymorphic deserialization vulnerability
- **CVE-2020-36518**: Denial of Service vulnerability
- **Current Version**: 2.17.1

**Action Required**:
1. Check if 2.17.1 includes fixes for these CVEs
2. Consider upgrading to latest 2.17.x or 2.18.x
3. Run OWASP Dependency-Check for confirmation

### 2.3 Spring Framework

**Status**: ✅ **LIKELY SAFE** - Version 6.1.5 is recent  
- Spring Framework 6.x is actively maintained
- Check Spring Security Advisories: https://spring.io/security

**Action Required**:
1. Verify against Spring Security Advisories
2. Check for any 6.1.5-specific CVEs

### 2.4 MySQL Connector

**Status**: ✅ **LIKELY SAFE** - Version 9.1.0 is current  
- MySQL Connector/J 9.x is actively maintained
- Check Oracle Security Advisories

**Action Required**:
1. Verify against Oracle Security Advisories
2. Check for any 9.1.0-specific CVEs

### 2.5 Apache Turbine/Torque/Fulcrum

**Status**: ⚠️ **VERIFY** - Legacy frameworks  
- These are mature, stable frameworks
- Check Apache Security Advisories: https://www.apache.org/security/

**Action Required**:
1. Verify against Apache Security Advisories
2. Check for any known CVEs in these versions

---

## 3. Customized Open Source Library Classes

### ⚠️ IMPORTANT: Custom Extensions Identified

The following classes **extend** open source library classes. These are **NOT modifications** of library source code, but **proper extensions** following framework patterns:

### 3.1 Turbine Action Extensions

#### `LoginUser.java`
- **Location**: `turbine-model-controller/src/main/java/modules/actions/LoginUser.java`
- **Extends**: `org.apache.turbine.modules.actions.LoginUser`
- **Customization**: 
  - Overrides `doPerform()` method
  - Adds anonymous user check
  - Custom reset logic
- **Risk Level**: ✅ **LOW** - Standard extension pattern
- **License Compliance**: ✅ Apache License 2.0 maintained

#### `LoginUserIntake.java`
- **Location**: `turbine-model-controller/src/main/java/modules/actions/LoginUserIntake.java`
- **Extends**: `org.apache.turbine.modules.actions.LoginUser`
- **Customization**: Intake-specific login handling
- **Risk Level**: ✅ **LOW** - Standard extension pattern

#### `FluxLogin.java`
- **Location**: `turbine-model-controller/src/main/java/flux/modules/actions/FluxLogin.java`
- **Extends**: `org.apache.turbine.modules.actions.LoginUser`
- **Customization**: Flux-specific login handling
- **Risk Level**: ✅ **LOW** - Standard extension pattern

### 3.2 Turbine Service Extensions

#### `SafeTurbinePullService.java`
- **Location**: `turbine-model-controller/src/main/java/com/uob/services/pull/SafeTurbinePullService.java`
- **Extends**: `org.apache.turbine.services.pull.TurbinePullService`
- **Customization**:
  - Overrides `populateContext()` method
  - Adds null user safety checks
  - Prevents NullPointerException for anonymous users
- **Risk Level**: ✅ **LOW** - Safety enhancement
- **License Compliance**: ✅ Apache License 2.0 maintained

### 3.3 Torque OM Extensions (Generated + Custom)

#### Record Mapper Classes
All Record Mapper classes extend base classes generated by Torque:

- `GtpUserRecordMapper` extends `BaseGtpUserRecordMapper`
- `GtpGroupRecordMapper` extends `BaseGtpGroupRecordMapper`
- `GtpRoleRecordMapper` extends `BaseGtpRoleRecordMapper`
- `GtpPermissionRecordMapper` extends `BaseGtpPermissionRecordMapper`
- `GtpUserGroupRoleRecordMapper` extends `BaseGtpUserGroupRoleRecordMapper`
- `GtpGroupRoleRecordMapper` extends `BaseGtpGroupRoleRecordMapper`
- `GtpRolePermissionRecordMapper` extends `BaseGtpRolePermissionRecordMapper`
- `TurbineUserRecordMapper` extends `BaseTurbineUserRecordMapper`
- `TurbineGroupRecordMapper` extends `BaseTurbineGroupRecordMapper`
- `TurbineRoleRecordMapper` extends `BaseTurbineRoleRecordMapper`
- `TurbinePermissionRecordMapper` extends `BaseTurbinePermissionRecordMapper`
- `TurbineUserGroupRoleRecordMapper` extends `BaseTurbineUserGroupRoleRecordMapper`
- `TurbineRolePermissionRecordMapper` extends `BaseTurbineRolePermissionRecordMapper`

**Risk Level**: ✅ **LOW** - Standard Torque ORM pattern (generated base + custom implementation)

### 3.4 Summary of Customizations

| Type | Count | Risk Level | License Compliance |
|------|-------|------------|-------------------|
| Action Extensions | 3 | ✅ LOW | ✅ Yes |
| Service Extensions | 1 | ✅ LOW | ✅ Yes |
| Record Mapper Extensions | 13 | ✅ LOW | ✅ Yes |
| **Total** | **17** | ✅ **LOW** | ✅ **Yes** |

**Key Points**:
- ✅ All customizations follow **standard extension patterns**
- ✅ **No source code modifications** to library classes
- ✅ **License compliance maintained** (Apache License 2.0)
- ✅ **Proper inheritance** - no library code copied

---

## 4. Security Recommendations

### 4.1 Immediate Actions (High Priority)

1. **Run OWASP Dependency-Check**
   ```bash
   mvn org.owasp:dependency-check-maven:check
   ```
   - Generates comprehensive CVE report
   - Identifies vulnerable transitive dependencies
   - Creates dependency-check-report.html

2. **Verify Jackson Version**
   - Check if 2.17.1 includes CVE fixes
   - Consider upgrading to 2.17.2+ or 2.18.x
   - Review: https://github.com/FasterXML/jackson-databind/security

3. **Check Spring Security Advisories**
   - Visit: https://spring.io/security
   - Verify 6.1.5 has no known CVEs
   - Subscribe to security notifications

4. **Verify MySQL Connector**
   - Check Oracle Security Advisories
   - Verify 9.1.0 has no known CVEs

### 4.2 Ongoing Actions (Medium Priority)

1. **Set Up Automated Scanning**
   - Integrate OWASP Dependency-Check into CI/CD
   - Use GitHub Dependabot or Snyk
   - Schedule monthly security scans

2. **Dependency Update Policy**
   - Review dependencies quarterly
   - Update to latest stable versions
   - Test thoroughly after updates

3. **Security Monitoring**
   - Subscribe to security advisories:
     - Apache Security: https://www.apache.org/security/
     - Spring Security: https://spring.io/security
     - Oracle Security: https://www.oracle.com/security-alerts/
     - NVD: https://nvd.nist.gov/

### 4.3 Best Practices (Low Priority)

1. **Documentation**
   - Maintain this security audit document
   - Update when dependencies change
   - Document any security patches applied

2. **Code Review**
   - Review all custom extensions for security issues
   - Ensure proper input validation
   - Check for SQL injection risks in custom queries

3. **Penetration Testing**
   - Conduct periodic security testing
   - Focus on authentication/authorization
   - Test REST API endpoints

---

## 5. Dependency Tree Analysis

### 5.1 Generate Dependency Tree

To get a complete dependency tree, run:

```bash
mvn dependency:tree > dependency-tree.txt
```

This will show all transitive dependencies that may have vulnerabilities.

### 5.2 Common Transitive Dependencies to Watch

Based on the frameworks used, watch for vulnerabilities in:

- **Apache Commons** libraries (commons-io, commons-lang3, etc.)
- **SLF4J** logging bridges
- **Jakarta** EE libraries
- **Velocity** template engine
- **Antlr** parser (used by Velocity)

---

## 6. License Compliance

### 6.1 License Summary

All dependencies use **Apache License 2.0** or compatible licenses:

- ✅ Apache Turbine: Apache License 2.0
- ✅ Apache Torque: Apache License 2.0
- ✅ Apache Fulcrum: Apache License 2.0
- ✅ Spring Framework: Apache License 2.0
- ✅ Jackson: Apache License 2.0
- ✅ Log4j2: Apache License 2.0
- ✅ JUnit: Eclipse Public License 2.0 (compatible)
- ✅ MySQL Connector: GPL 2.0 (with Classpath Exception)

**Status**: ✅ **COMPLIANT** - All licenses are compatible with Apache License 2.0

---

## 7. Action Items Checklist

### Immediate (This Week)

- [ ] Run OWASP Dependency-Check: `mvn org.owasp:dependency-check-maven:check`
- [ ] Review dependency-check-report.html
- [ ] Verify Jackson 2.17.1 CVE status
- [ ] Check Spring Security Advisories for 6.1.5
- [ ] Verify MySQL Connector 9.1.0 CVE status

### Short Term (This Month)

- [ ] Set up automated dependency scanning in CI/CD
- [ ] Create dependency update schedule
- [ ] Subscribe to security advisories
- [ ] Document any vulnerabilities found

### Long Term (Ongoing)

- [ ] Quarterly dependency review
- [ ] Monthly security scans
- [ ] Annual penetration testing
- [ ] Update this audit document quarterly

---

## 8. Tools and Resources

### 8.1 Security Scanning Tools

1. **OWASP Dependency-Check**
   - Maven Plugin: `org.owasp:dependency-check-maven`
   - Website: https://owasp.org/www-project-dependency-check/

2. **Snyk**
   - Website: https://snyk.io/
   - Free tier available

3. **GitHub Dependabot**
   - Integrated with GitHub
   - Automatic PRs for updates

4. **Sonatype OSS Index**
   - Website: https://ossindex.sonatype.org/
   - Free API available

### 8.2 Security Advisory Sources

1. **National Vulnerability Database (NVD)**
   - Website: https://nvd.nist.gov/
   - CVE database

2. **Apache Security**
   - Website: https://www.apache.org/security/

3. **Spring Security Advisories**
   - Website: https://spring.io/security

4. **Oracle Security Alerts**
   - Website: https://www.oracle.com/security-alerts/

---

## 9. Conclusion

### Summary

- ✅ **Log4j2**: Secure (2.23.1 - post-CVE)
- ⚠️ **Jackson**: Requires verification (2.17.1)
- ✅ **Spring Framework**: Likely secure (6.1.5 - recent)
- ✅ **MySQL Connector**: Likely secure (9.1.0 - current)
- ⚠️ **Apache Libraries**: Requires verification
- ✅ **Customizations**: All follow proper extension patterns
- ✅ **License Compliance**: All compatible

### Overall Risk Assessment

**Current Status**: ⚠️ **REQUIRES VERIFICATION**

The project uses recent, actively maintained libraries. However, **automated CVE scanning is required** to identify any vulnerabilities in:
- Direct dependencies
- Transitive dependencies
- Library combinations

### Next Steps

1. **Run OWASP Dependency-Check** (highest priority)
2. **Review and address any CVEs found**
3. **Set up automated scanning**
4. **Update this document with findings**

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-24  
**Next Review**: 2026-04-24 (Quarterly)

---

## Appendix A: Maven Commands for Security Analysis

```bash
# Generate dependency tree
mvn dependency:tree > dependency-tree.txt

# Run OWASP Dependency-Check
mvn org.owasp:dependency-check-maven:check

# List all dependencies
mvn dependency:list

# Copy all dependencies (for analysis)
mvn dependency:copy-dependencies

# Check for updates
mvn versions:display-dependency-updates
```

---

## Appendix B: Customized Classes Reference

### Complete List of Custom Extensions

1. `LoginUser` - Extends `org.apache.turbine.modules.actions.LoginUser`
2. `LoginUserIntake` - Extends `org.apache.turbine.modules.actions.LoginUser`
3. `FluxLogin` - Extends `org.apache.turbine.modules.actions.LoginUser`
4. `SafeTurbinePullService` - Extends `org.apache.turbine.services.pull.TurbinePullService`
5. All Record Mapper classes (13 classes) - Extend Torque-generated base classes

**Total**: 17 customized classes, all following proper extension patterns.

---

*End of Security Audit Report*

# Oracle Database & JBoss 8 Deployment Guide

This guide provides instructions for deploying the UOB Turbine7 Portal MM application with Oracle database on JBoss 8 (WildFly 32+).

## Overview

The application now supports:
- **Oracle Database** connectivity (in addition to MySQL)
- **JBoss 8** deployment (in addition to Tomcat)

## Prerequisites

### Oracle Database
- Oracle Database 12c or later
- Oracle JDBC Driver (ojdbc11.jar)
- SQL*Plus for running setup scripts

### JBoss 8
- JBoss 8 (WildFly 32+) installed
- Java 17 or later
- Oracle JDBC driver module installed

## Step 1: Setup Oracle Database

### Option A: Automated Setup (Recommended)

**Windows:**
```powershell
cd C:\mysql81\uob-turbine-portal-mm-oracle
.\setup-database.ps1
```

**Linux/Mac:**
```bash
cd /path/to/uob-turbine-portal-mm-oracle
chmod +x setup-database.sh
./setup-database.sh
```

### Option B: Manual Setup

1. **Create Schema and User:**
   ```bash
   sqlplus sys/password@localhost:1521/ORCL as sysdba @01-create-schema-and-user.sql
   ```

2. **Create Tables:**
   ```bash
   sqlplus kishore/Kish1381@localhost:1521/ORCL @02-create-tables.sql
   ```

3. **Load Test Data:**
   ```bash
   sqlplus kishore/Kish1381@localhost:1521/ORCL @03-load-test-data.sql
   ```

4. **Verify Setup:**
   ```bash
   sqlplus kishore/Kish1381@localhost:1521/ORCL @04-verify-setup.sql
   ```

## Step 2: Install Oracle JDBC Driver in JBoss 8

### Method 1: Using JBoss CLI

```bash
# Start JBoss CLI
$JBOSS_HOME/bin/jboss-cli.sh --connect

# Install Oracle JDBC driver module
module add --name=com.oracle.ojdbc11 \
  --resources=/path/to/ojdbc11.jar \
  --dependencies=javax.api,javax.transaction.api

# Exit CLI
quit
```

### Method 2: Manual Installation

1. Create directory structure:
   ```bash
   mkdir -p $JBOSS_HOME/modules/com/oracle/ojdbc11/main
   ```

2. Copy Oracle JDBC driver:
   ```bash
   cp ojdbc11.jar $JBOSS_HOME/modules/com/oracle/ojdbc11/main/
   ```

3. Create `module.xml`:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <module xmlns="urn:jboss:module:1.9" name="com.oracle.ojdbc11">
       <resources>
           <resource-root path="ojdbc11.jar"/>
       </resources>
       <dependencies>
           <module name="javax.api"/>
           <module name="javax.transaction.api"/>
       </dependencies>
   </module>
   ```

## Step 3: Configure Oracle Data Source in JBoss 8

### Method 1: Using JBoss CLI

```bash
$JBOSS_HOME/bin/jboss-cli.sh --connect

# Add Oracle data source
/subsystem=datasources/jdbc-driver=oracle:add( \
  driver-name=oracle, \
  driver-module-name=com.oracle.ojdbc11, \
  driver-class-name=oracle.jdbc.OracleDriver)

# Create data source
/subsystem=datasources/data-source=kishore:add( \
  jndi-name=java:jboss/datasources/kishore, \
  driver-name=oracle, \
  connection-url=jdbc:oracle:thin:@localhost:1521:ORCL, \
  user-name=KISHORE, \
  password=Kish1381@, \
  enabled=true)

# Test connection
/subsystem=datasources/data-source=kishore:test-connection-in-pool

quit
```

### Method 2: Edit standalone.xml

Edit `$JBOSS_HOME/standalone/configuration/standalone.xml`:

```xml
<subsystem xmlns="urn:jboss:domain:datasources:6.0">
    <drivers>
        <driver name="oracle" module="com.oracle.ojdbc11">
            <driver-class>oracle.jdbc.OracleDriver</driver-class>
        </driver>
    </drivers>
    <datasources>
        <datasource jndi-name="java:jboss/datasources/kishore" 
                    pool-name="kishore" 
                    enabled="true">
            <connection-url>jdbc:oracle:thin:@localhost:1521:ORCL</connection-url>
            <driver>oracle</driver>
            <security>
                <user-name>KISHORE</user-name>
                <password>Kish1381@</password>
            </security>
            <pool>
                <min-pool-size>5</min-pool-size>
                <max-pool-size>20</max-pool-size>
            </pool>
        </datasource>
    </datasources>
</subsystem>
```

## Step 4: Build Application

### Build for JBoss 8 + Oracle

```bash
cd C:\Turbineprojects\uob-turbine7-portal-mm
mvn clean package -P jboss8-oracle -DskipTests
```

This will:
- Use `web-jboss.xml` instead of `web.xml`
- Include Oracle JDBC driver in WAR
- Use `Torque-oracle.properties` for database configuration
- Generate WAR: `uob-portal-mm-jboss-oracle.war`

## Step 5: Deploy to JBoss 8

### Option A: Using Deployment Script

```powershell
.\deploy-jboss8.ps1 -JbossHome "C:\jboss" -WarFile "webapp\target\uob-portal-mm-jboss-oracle.war"
```

### Option B: Manual Deployment

1. **Copy WAR to deployments:**
   ```bash
   cp webapp/target/uob-portal-mm-jboss-oracle.war \
      $JBOSS_HOME/standalone/deployments/
   ```

2. **Create deployment marker:**
   ```bash
   touch $JBOSS_HOME/standalone/deployments/uob-portal-mm-jboss-oracle.war.dodeploy
   ```

3. **Start JBoss (if not running):**
   ```bash
   $JBOSS_HOME/bin/standalone.sh
   ```

## Step 6: Verify Deployment

### Check Deployment Status

```bash
$JBOSS_HOME/bin/jboss-cli.sh --connect

# Check deployment status
/deployment=uob-portal-mm-jboss-oracle.war:read-resource

# Check data source status
/subsystem=datasources/data-source=kishore:read-resource

quit
```

### Access Application

- **Application URL:** http://localhost:8080/uob-portal-mm-jboss-oracle/app
- **REST API:** http://localhost:8080/uob-portal-mm-jboss-oracle/api

### Test Login

Use default test users:
- **Username:** admin / **Password:** password123
- **Username:** manager1 / **Password:** password123
- **Username:** user1 / **Password:** password123

## Troubleshooting

### Issue: Data Source Not Found

**Solution:**
1. Verify data source is configured:
   ```bash
   /subsystem=datasources/data-source=kishore:read-resource
   ```
2. Test connection:
   ```bash
   /subsystem=datasources/data-source=kishore:test-connection-in-pool
   ```

### Issue: Oracle JDBC Driver Not Found

**Solution:**
1. Verify driver module is installed:
   ```bash
   /subsystem=datasources/jdbc-driver=oracle:read-resource
   ```
2. Check module exists:
   ```bash
   ls $JBOSS_HOME/modules/com/oracle/ojdbc11/main/
   ```

### Issue: Deployment Fails

**Solution:**
1. Check JBoss logs:
   ```bash
   tail -f $JBOSS_HOME/standalone/log/server.log
   ```
2. Check deployment status:
   ```bash
   ls -la $JBOSS_HOME/standalone/deployments/*.war*
   ```
3. Remove failed deployment marker:
   ```bash
   rm $JBOSS_HOME/standalone/deployments/*.failed
   ```

### Issue: Connection Refused

**Solution:**
1. Verify Oracle database is running
2. Check Oracle listener:
   ```bash
   lsnrctl status
   ```
3. Test connection from JBoss server:
   ```bash
   sqlplus KISHORE/Kish1381@localhost:1521/ORCL
   ```

## Maven Profiles Summary

| Profile | Application Server | Database | WAR Name |
|---------|-------------------|----------|----------|
| tomcat-mysql | Tomcat | MySQL | uob-t7-portal-mm-tomcat.war |
| tomcat-oracle | Tomcat | Oracle | uob-t7-portal-mm-tomcat-oracle.war |
| jboss8-mysql | JBoss 8 | MySQL | uob-portal-mm-jboss-mysql.war |
| jboss8-oracle | JBoss 8 | Oracle | uob-portal-mm-jboss-oracle.war |

## Configuration Files

- **Torque.properties** - MySQL database configuration
- **Torque-oracle.properties** - Oracle database configuration
- **web.xml** - Tomcat web descriptor
- **web-jboss.xml** - JBoss 8 web descriptor

## Additional Resources

- **Oracle Setup:** `C:\mysql81\uob-turbine-portal-mm-oracle\README.md`
- **Quick Start:** `C:\mysql81\uob-turbine-portal-mm-oracle\QUICK-START.md`
- **Project Root:** `C:\Turbineprojects\uob-turbine7-portal-mm`

## Support

For issues or questions:
1. Check application logs: `$JBOSS_HOME/standalone/log/server.log`
2. Check JBoss deployment logs: `$JBOSS_HOME/standalone/log/deployment.log`
3. Review Oracle setup documentation
4. Verify database connectivity

---

**Last Updated:** January 2026  
**Maintained By:** UOB Development Team

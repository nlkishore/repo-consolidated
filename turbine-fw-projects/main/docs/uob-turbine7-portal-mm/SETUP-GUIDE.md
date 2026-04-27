# Setup Guide - Multi-Module Project

## Complete Setup Instructions

This guide provides step-by-step instructions to set up and build the multi-module project from scratch.

## Prerequisites

1. **Java 17** or higher installed
2. **Maven 3.6** or higher installed
3. **MySQL 8.x** installed and running
4. **Git** (optional, for version control)

## Step 1: Verify Project Structure

Ensure you have the following structure:
```
uob-turbine7-portal-mm/
├── pom.xml
├── torque-orm/
├── turbine-model-controller/
├── spring-rest-api/
└── webapp/
```

## Step 2: Generate Base Classes

**CRITICAL**: Base classes must be generated before building.

### Option A: Using PowerShell Script (Windows)

```powershell
cd torque-orm
.\generate-base-classes.ps1
cd ..
```

### Option B: Using Bash Script (Linux/Mac)

```bash
cd torque-orm
chmod +x generate-base-classes.sh
./generate-base-classes.sh
cd ..
```

### Option C: Manual Process

1. Backup modifiable classes:
```bash
cd torque-orm
mkdir -p src/main/java-backup
cp src/main/java/com/uob/om/Gtp*.java src/main/java-backup/
cp src/main/java/com/uob/om/Turbine*.java src/main/java-backup/
```

2. Remove modifiable classes:
```bash
rm src/main/java/com/uob/om/Gtp*.java
rm src/main/java/com/uob/om/Turbine*.java
```

3. Generate base classes:
```bash
mvn clean generate-sources -DskipTests
```

4. Restore modifiable classes:
```bash
cp src/main/java-backup/*.java src/main/java/com/uob/om/
rm -rf src/main/java-backup
cd ..
```

## Step 3: Verify Base Classes

Check that base classes were generated:

```bash
ls torque-orm/target/generated-sources/com/uob/om/Base*.java
```

You should see multiple `Base*.java` files.

## Step 4: Build All Modules

```bash
mvn clean install -DskipTests
```

This will:
1. Build `torque-orm` module
2. Build `turbine-model-controller` module
3. Build `spring-rest-api` module
4. Build `webapp` module

## Step 5: Setup Database

1. Create database:
```sql
CREATE DATABASE kishore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Create user:
```sql
CREATE USER 'kishore'@'localhost' IDENTIFIED BY 'Kish1381@';
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost';
FLUSH PRIVILEGES;
```

3. Execute schema:
```bash
mysql -u kishore -p kishore < torque-orm/target/generated-sql/torque/mysql/gtp-security-schema.sql
```

4. Load test data (optional):
```bash
mysql -u kishore -p kishore < webapp/src/main/data/gtp-test-data.sql
```

## Step 6: Build WAR File

### For Tomcat:
```bash
mvn clean package -DskipTests -Ptomcat
```

Output: `webapp/target/uob-t7-portal-tomcat.war`

### For JBoss 8:
```bash
mvn clean package -DskipTests -Pjboss8
```

Output: `webapp/target/uob-t7-portal-jboss8.war`

## Step 7: Deploy

### Tomcat:
```bash
cp webapp/target/uob-t7-portal-tomcat.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

### JBoss 8:
```bash
cp webapp/target/uob-t7-portal-jboss8.war $JBOSS_HOME/standalone/deployments/
$JBOSS_HOME/bin/standalone.sh
```

## Troubleshooting

### "cannot find symbol: class BaseGtpUser"

**Solution**: Base classes not generated. Run:
```bash
cd torque-orm
.\generate-base-classes.ps1  # Windows
# OR
./generate-base-classes.sh  # Linux/Mac
cd ..
mvn clean install -DskipTests
```

### "Module not found" errors

**Solution**: Install modules to local repository:
```bash
mvn clean install -DskipTests
```

### Database connection errors

**Solution**: 
1. Verify MySQL is running
2. Check `webapp/src/main/webapp/WEB-INF/conf/Torque.properties`
3. Ensure database and user exist

## Project Independence

✅ **This project is completely independent**:
- No references to original `uob-turbine7-portal` project
- All paths are relative
- Can be built from any location
- Portable across machines

## Quick Reference

```bash
# Full setup
cd torque-orm
.\generate-base-classes.ps1
cd ..
mvn clean install -DskipTests
mvn clean package -DskipTests -Ptomcat
```

## Documentation

- **[BUILD-INSTRUCTIONS.md](BUILD-INSTRUCTIONS.md)** - Detailed build instructions
- **[PROJECT-DOCUMENTATION.md](PROJECT-DOCUMENTATION.md)** - Complete documentation
- **[TORQUE-BASE-CLASSES.md](TORQUE-BASE-CLASSES.md)** - Base class generation guide
- **[QUICK-START.md](QUICK-START.md)** - Quick start guide

# Multi-Module Project Build Instructions

## Project Overview

This is a **standalone multi-module Maven project** for the UOB Turbine 7 Portal application. The project is completely independent and can be built and deployed from any machine or location.

## Project Structure

```
uob-turbine7-portal-mm/
├── pom.xml                          # Parent POM
├── torque-orm/                      # Module 1: Torque ORM
│   ├── pom.xml
│   ├── generate-base-classes.ps1   # Script to generate base classes
│   └── src/
│       ├── main/
│       │   ├── java/com/uob/om/     # OM classes (GtpUser, GtpRole, etc.)
│       │   └── torque-schema/       # Torque schema definitions
│       └── test/                    # Unit tests
├── turbine-model-controller/        # Module 2: Turbine MVC
├── spring-rest-api/                 # Module 3: Spring REST API
└── webapp/                          # Module 4: Web Application
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.x (for database)
- Apache Tomcat 10.x or JBoss 8 (WildFly) for deployment

## Building the Project

### Step 1: Generate Base Classes (IMPORTANT)

**Torque Limitation**: When modifiable classes (Gtp*.java, Turbine*.java) already exist in `src/main/java`, Torque does not automatically generate base classes. You need to generate them first.

#### Option A: Using PowerShell Script (Recommended for Windows)

```powershell
cd torque-orm
.\generate-base-classes.ps1
cd ..
```

#### Option B: Using Bash Script (Linux/Mac)

```bash
cd torque-orm
chmod +x generate-base-classes.sh
./generate-base-classes.sh
cd ..
```

#### Option C: Manual Process

1. Temporarily move modifiable classes:
```bash
cd torque-orm
mkdir -p src/main/java-backup
mv src/main/java/com/uob/om/Gtp*.java src/main/java-backup/
mv src/main/java/com/uob/om/Turbine*.java src/main/java-backup/
```

2. Generate base classes:
```bash
mvn clean generate-sources -DskipTests
```

3. Restore modifiable classes:
```bash
mv src/main/java-backup/*.java src/main/java/com/uob/om/
rm -rf src/main/java-backup
cd ..
```

### Step 2: Build All Modules

After base classes are generated:

```bash
mvn clean install -DskipTests
```

This builds all modules in the correct order:
1. `torque-orm` (base module)
2. `turbine-model-controller` (depends on torque-orm)
3. `spring-rest-api` (depends on torque-orm, turbine-model-controller)
4. `webapp` (depends on all modules)

### Step 3: Build WAR Files

#### For Apache Tomcat (default profile):
```bash
mvn clean package -DskipTests -Ptomcat
```
Output: `webapp/target/uob-t7-portal-tomcat.war`

#### For JBoss 8 (WildFly):
```bash
mvn clean package -DskipTests -Pjboss8
```
Output: `webapp/target/uob-t7-portal-jboss8.war`

## Module Dependencies

```
webapp
  ├── spring-rest-api
  │   ├── turbine-model-controller
  │   │   └── torque-orm
  │   └── torque-orm
  ├── turbine-model-controller
  │   └── torque-orm
  └── torque-orm
```

## Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE kishore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Create database user:
```sql
CREATE USER 'kishore'@'localhost' IDENTIFIED BY 'Kish1381@';
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost';
FLUSH PRIVILEGES;
```

3. Execute generated SQL schema:
```bash
mysql -u kishore -p kishore < torque-orm/target/generated-sql/torque/mysql/gtp-security-schema.sql
```

4. Load test data (optional):
```bash
mysql -u kishore -p kishore < webapp/src/main/data/gtp-test-data.sql
```

## Deployment

### Apache Tomcat

1. Copy WAR file to Tomcat webapps directory:
```bash
cp webapp/target/uob-t7-portal-tomcat.war $CATALINA_HOME/webapps/
```

2. Start Tomcat:
```bash
$CATALINA_HOME/bin/startup.sh
```

3. Access application:
- Web UI: `http://localhost:8080/uob-t7-portal-tomcat/`
- REST API: `http://localhost:8080/uob-t7-portal-tomcat/api/users`

### JBoss 8 (WildFly)

1. Copy WAR file to JBoss deployment directory:
```bash
cp webapp/target/uob-t7-portal-jboss8.war $JBOSS_HOME/standalone/deployments/
```

2. Start JBoss:
```bash
$JBOSS_HOME/bin/standalone.sh
```

3. Access application:
- Web UI: `http://localhost:8080/uob-t7-portal-jboss8/`
- REST API: `http://localhost:8080/uob-t7-portal-jboss8/api/users`

## Configuration

### Database Connection

Edit `webapp/src/main/webapp/WEB-INF/conf/Torque.properties`:
```properties
torque.database.default=kishore
torque.database.kishore.adapter=mysql
torque.database.kishore.url=jdbc:mysql://localhost:3306/kishore?allowPublicKeyRetrieval=true
torque.database.kishore.user=kishore
torque.database.kishore.password=Kish1381@
```

### Application Server Port

- Tomcat: Configure in `server.xml` (default: 8080)
- JBoss: Configure in `standalone.xml` (default: 8080)

## REST API Endpoints

After deployment, the following REST endpoints are available:

- **Users**: `GET /api/users` - List all users
- **Groups**: `GET /api/groups` - List all groups
- **Roles**: `GET /api/roles` - List all roles
- **Permissions**: `GET /api/permissions` - List all permissions

API documentation (Swagger): `http://localhost:8080/{context}/swagger-ui.html`

## Troubleshooting

### Base Classes Not Found

If you see "cannot find symbol" errors for Base* classes:

1. **Run the base class generation script**:
```powershell
cd torque-orm
.\generate-base-classes.ps1
cd ..
```

2. **Verify base classes exist**:
```bash
ls torque-orm/target/generated-sources/com/uob/om/Base*.java
```

3. **Rebuild**:
```bash
mvn clean install -DskipTests
```

### Database Connection Issues

1. Verify MySQL is running:
```bash
mysql -u kishore -p
```

2. Check connection URL in `Torque.properties`

3. Ensure `allowPublicKeyRetrieval=true` is in JDBC URL for MySQL 8.x

### Module Dependency Errors

If modules can't find each other:

1. Install modules to local repository:
```bash
mvn clean install -DskipTests
```

2. Verify module dependencies in each `pom.xml`

## Development

### Running Tests

```bash
mvn test
```

### Code Coverage

```bash
mvn test jacoco:report
```

View report: `target/site/jacoco/index.html`

### Building Specific Module

```bash
mvn clean install -pl torque-orm -am
```

Where:
- `-pl` specifies the module
- `-am` builds dependencies as well

## Project Portability

This project is completely portable and can be:

- Cloned to any location
- Built on any machine with Java and Maven
- Deployed to any compatible application server

**No external dependencies on other projects or absolute paths are required.**

## Quick Reference

```bash
# Full build process
cd torque-orm
.\generate-base-classes.ps1    # Windows
# OR
./generate-base-classes.sh     # Linux/Mac
cd ..
mvn clean install -DskipTests
mvn clean package -DskipTests -Ptomcat
```

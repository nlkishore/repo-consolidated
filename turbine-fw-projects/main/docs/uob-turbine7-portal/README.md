# UOB Turbine 7 Portal

A web application integrating Apache Turbine 7 with Spring Framework 6.x, providing REST APIs for security management (users, groups, roles, and permissions) while maintaining compatibility with existing Turbine Action classes and Torque ORM.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Build Instructions](#build-instructions)
- [REST API Endpoints](#rest-api-endpoints)
- [Deployment Instructions](#deployment-instructions)
- [Web URLs](#web-urls)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [Troubleshooting](#troubleshooting)

## Overview

This project combines:
- **Apache Turbine 7**: Legacy web application framework
- **Spring Framework 6.x**: Modern REST API layer
- **Torque ORM**: Database persistence layer
- **Fulcrum Security**: Security framework integrated with Turbine
- **Jakarta EE 10**: Enterprise Java standards

The application exposes REST APIs for managing security entities while reusing existing Turbine Action classes as the service layer.

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6.0 or higher
- **MySQL**: 8.0 or higher
- **Apache Tomcat**: 10.1.x (for deployment)
- **Database User**: `kishore` with password `Kish1381@` (or configure your own)

## Database Setup

### 1. Create Database and User

```sql
CREATE DATABASE kishore;
CREATE USER 'kishore'@'localhost' IDENTIFIED BY 'Kish1381@';
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Database Tables

The application uses the following security tables:
- `GTP_USER` - User accounts
- `GTP_GROUP` - User groups
- `GTP_ROLE` - Security roles
- `GTP_PERMISSION` - Permissions
- `GTP_USER_GROUP_ROLE` - User-Group-Role mappings
- `GTP_GROUP_ROLE` - Group-Role mappings
- `GTP_ROLE_PERMISSION` - Role-Permission mappings

### 3. Initialize Database Schema

The database schema is generated from Torque schema files located in `src/main/torque-schema/`. The schema is automatically created during the build process.

### 4. Load Test Data

Test data can be loaded from:
- `src/main/data/gtp-test-data.sql`
- `src/main/data/sample-mysql-data/`

## Build Instructions

### 1. Clone and Navigate

```bash
cd c:\Turbineprojects\uob-turbine7-portal
```

### 2. Build the Project

```bash
mvn clean install
```

This will:
- Generate Torque ORM classes from schema
- Compile Java source code
- Run tests
- Package WAR file: `target/uob-t7-portal.war`

### 3. Build Without Tests

```bash
mvn clean install -DskipTests
```

### 4. Generate Database Schema SQL

```bash
mvn torque:generate
```

Generated SQL files will be in `target/generated-sql/torque/mysql/`

## REST API Endpoints

All REST APIs are available under the `/api` context path. The APIs return JSON responses.

### Base URL
```
http://localhost:8080/uob-t7-portal/api
```

### User Endpoints

#### Get All Users
```
GET /api/users
```
**Response:** List of all users
```json
[
  {
    "userId": 1,
    "loginName": "admin",
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@example.com",
    "confirmed": true,
    "lastLogin": "2026-01-23T10:00:00",
    "created": "2026-01-01T00:00:00",
    "modified": "2026-01-23T10:00:00"
  }
]
```

#### Get User by Login Name
```
GET /api/users/{loginName}
```
**Example:** `GET /api/users/admin`
**Response:** User details
```json
{
  "userId": 1,
  "loginName": "admin",
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@example.com",
  "confirmed": true,
  "lastLogin": "2026-01-23T10:00:00",
  "created": "2026-01-01T00:00:00",
  "modified": "2026-01-23T10:00:00"
}
```

### Group Endpoints

#### Get All Groups
```
GET /api/groups
```
**Response:** List of all groups
```json
[
  {
    "groupId": 1,
    "groupName": "Administrators"
  }
]
```

#### Get Group by Name
```
GET /api/groups/{groupName}
```
**Example:** `GET /api/groups/Administrators`
**Response:** Group details
```json
{
  "groupId": 1,
  "groupName": "Administrators"
}
```

### Role Endpoints

#### Get All Roles
```
GET /api/roles
```
**Response:** List of all roles
```json
[
  {
    "roleId": 1,
    "roleName": "admin"
  }
]
```

#### Get Role by Name
```
GET /api/roles/{roleName}
```
**Example:** `GET /api/roles/admin`
**Response:** Role details
```json
{
  "roleId": 1,
  "roleName": "admin"
}
```

#### Get Permissions for a Role
```
GET /api/roles/{roleName}/permissions
```
**Example:** `GET /api/roles/admin/permissions`
**Response:** List of permission names
```json
[
  "user.create",
  "user.delete",
  "user.update"
]
```

### Permission Endpoints

#### Get All Permissions
```
GET /api/permissions
```
**Response:** List of all permissions
```json
[
  {
    "permissionId": 1,
    "permissionName": "user.create"
  }
]
```

#### Get Permission by Name
```
GET /api/permissions/{permissionName}
```
**Example:** `GET /api/permissions/user.create`
**Response:** Permission details
```json
{
  "permissionId": 1,
  "permissionName": "user.create"
}
```

### API Response Codes

- `200 OK` - Successful request
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Deployment Instructions

### Option 1: Deploy to Apache Tomcat

#### 1. Copy WAR File

```bash
copy target\uob-t7-portal.war C:\applicationservers\apache-tomcat-10.1.44\webapps\uob-t7-portal.war
```

#### 2. Configure DataSource (JNDI)

Edit `C:\applicationservers\apache-tomcat-10.1.44\conf\context.xml` or create `conf\Catalina\localhost\uob-t7-portal.xml`:

```xml
<Context>
    <Resource name="jdbc/kishore"
              auth="Container"
              type="javax.sql.DataSource"
              driverClassName="com.mysql.cj.jdbc.Driver"
              url="jdbc:mysql://localhost:3306/kishore?useSSL=false&amp;serverTimezone=UTC"
              username="kishore"
              password="Kish1381@"
              maxTotal="20"
              maxIdle="10"
              maxWaitMillis="10000"/>
</Context>
```

#### 3. Add MySQL Driver

Copy MySQL JDBC driver to Tomcat's `lib` directory:
```bash
copy mysql-connector-j-8.0.33.jar C:\applicationservers\apache-tomcat-10.1.44\lib\
```

#### 4. Start Tomcat

```bash
cd C:\applicationservers\apache-tomcat-10.1.44\bin
startup.bat
```

#### 5. Verify Deployment

Check Tomcat logs: `C:\applicationservers\apache-tomcat-10.1.44\logs\catalina.out`

### Option 2: Maven Tomcat Plugin (Development)

The project includes Maven Antrun plugin for deployment. Configure in `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <executions>
        <execution>
            <phase>install</phase>
            <goals>
                <goal>run</goal>
            </goals>
            <configuration>
                <target>
                    <copy file="${project.build.directory}/uob-t7-portal.war"
                          tofile="C:/applicationservers/apache-tomcat-10.1.44/webapps/uob-t7-portal.war"/>
                </target>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Deploy with:
```bash
mvn clean install
```

### Option 3: Docker Deployment

See `docs/DOCKER-PODMAN-COMPOSE-README.md` for Docker deployment instructions.

## Web URLs

After successful deployment, access the application at:

### Turbine Web Application
```
http://localhost:8080/uob-t7-portal/app
```
- Main Turbine application interface
- Login page and user management screens
- Traditional Turbine MVC pages

### REST API Base
```
http://localhost:8080/uob-t7-portal/api
```

### REST API Examples

- **All Users:** `http://localhost:8080/uob-t7-portal/api/users`
- **User by Login:** `http://localhost:8080/uob-t7-portal/api/users/admin`
- **All Groups:** `http://localhost:8080/uob-t7-portal/api/groups`
- **Group by Name:** `http://localhost:8080/uob-t7-portal/api/groups/Administrators`
- **All Roles:** `http://localhost:8080/uob-t7-portal/api/roles`
- **Role by Name:** `http://localhost:8080/uob-t7-portal/api/roles/admin`
- **Role Permissions:** `http://localhost:8080/uob-t7-portal/api/roles/admin/permissions`
- **All Permissions:** `http://localhost:8080/uob-t7-portal/api/permissions`
- **Permission by Name:** `http://localhost:8080/uob-t7-portal/api/permissions/user.create`

### API Documentation (SpringDoc OpenAPI)

If SpringDoc is configured:
```
http://localhost:8080/uob-t7-portal/api/swagger-ui.html
http://localhost:8080/uob-t7-portal/api/v3/api-docs
```

## Configuration

### Database Configuration

Database connection is configured via JNDI resource `jdbc/kishore` in:
- `src/main/webapp/META-INF/context.xml` (for embedded/development)
- Tomcat's `conf/context.xml` or `conf/Catalina/localhost/` (for production)

### Turbine Configuration

Main configuration file: `src/main/webapp/WEB-INF/conf/TurbineResources.properties`

Key settings:
- Database connection pool
- Security service configuration
- Logging configuration

### Spring Configuration

Spring is configured via:
- `com.uob.config.SpringConfig` - Main Spring configuration class
- `com.uob.config.SpringWebAppInitializer` - Servlet initialization
- Component scanning: `com.uob.controller`, `com.uob.service`

### Web Application Descriptor

`src/main/webapp/WEB-INF/web.xml`:
- Turbine servlet mapped to `/app/*`
- Spring DispatcherServlet mapped to `/api/*`
- Session timeout: 60 minutes

## Architecture

### Technology Stack

- **Framework:** Apache Turbine 7 + Spring Framework 6.x
- **ORM:** Apache Torque
- **Security:** Apache Fulcrum Security
- **Database:** MySQL 8.0+
- **Application Server:** Apache Tomcat 10.1.x
- **Java Version:** JDK 17+
- **Build Tool:** Maven 3.6+

### Project Structure

```
uob-t7-portal/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/uob/
│   │   │       ├── config/          # Spring configuration
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── service/          # Service layer (Turbine integration)
│   │   │       │   └── adapter/     # User adapter for type conversion
│   │   │       ├── om/              # Torque-generated Object Model
│   │   │       └── modules/         # Turbine modules (screens, actions)
│   │   ├── torque-schema/           # Database schema definitions
│   │   ├── webapp/
│   │   │   ├── WEB-INF/
│   │   │   │   ├── conf/           # Turbine configuration
│   │   │   │   └── web.xml         # Web application descriptor
│   │   │   └── templates/          # Velocity templates
│   │   └── data/                   # SQL test data
│   └── test/
├── target/                          # Build output
│   ├── uob-t7-portal.war
│   └── generated-sources/           # Torque-generated classes
└── pom.xml
```

### Integration Pattern

The application uses an **Adapter/Shim pattern** to bridge between:
- `org.apache.turbine.om.security.User` (Turbine interface)
- `org.apache.fulcrum.security.entity.User` (Fulcrum interface)

The `UserAdapter` class (`com.uob.service.adapter.UserAdapter`) provides seamless conversion between these type systems.

### Service Layer

`TurbineSecurityService` (`com.uob.service.TurbineSecurityService`) wraps Turbine's `SecurityService` and provides methods for:
- User management
- Group management
- Role management
- Permission management

This service layer is reused by both Turbine Action classes and Spring REST controllers.

## Troubleshooting

### Build Issues

**Problem:** Maven build fails with compilation errors
**Solution:** 
- Ensure JDK 17+ is installed and `JAVA_HOME` is set
- Run `mvn clean install` to rebuild from scratch
- Check that MySQL driver is available in Maven repository

**Problem:** Torque generation fails
**Solution:**
- Verify schema files in `src/main/torque-schema/` are valid XML
- Check Maven Torque plugin version compatibility
- Run `mvn torque:generate` separately to see detailed errors

### Deployment Issues

**Problem:** WAR file doesn't deploy
**Solution:**
- Check Tomcat logs: `logs/catalina.out`
- Verify WAR file is not corrupted: `target/uob-t7-portal.war`
- Ensure Tomcat has sufficient memory (set `CATALINA_OPTS=-Xmx512m`)

**Problem:** Database connection errors
**Solution:**
- Verify MySQL is running: `mysql -u kishore -p`
- Check JNDI resource configuration in `context.xml`
- Ensure MySQL JDBC driver is in Tomcat `lib/` directory
- Verify database name, username, and password are correct

### Runtime Issues

**Problem:** REST APIs return 404
**Solution:**
- Verify Spring DispatcherServlet is mapped to `/api/*` in `web.xml`
- Check that `SpringConfig` is properly configured
- Verify component scanning includes `com.uob.controller` package
- Check application logs for Spring initialization errors

**Problem:** REST APIs return 500 errors
**Solution:**
- Check application logs for exceptions
- Verify database connection is working
- Ensure test data is loaded in database
- Check that Turbine services are properly initialized

**Problem:** Turbine pages don't load
**Solution:**
- Verify Turbine servlet is mapped to `/app/*` in `web.xml`
- Check `TurbineResources.properties` configuration
- Ensure Velocity templates are in `src/main/webapp/templates/`
- Check Turbine initialization logs

### Database Issues

**Problem:** Tables don't exist
**Solution:**
- Run Torque SQL generation: `mvn torque:generate`
- Execute generated SQL files from `target/generated-sql/torque/mysql/`
- Or load test data: `mysql -u kishore -p kishore < src/main/data/gtp-test-data.sql`

**Problem:** Foreign key constraint errors
**Solution:**
- Ensure tables are created in correct order
- Check that referenced IDs exist before inserting
- Verify test data relationships are correct

## Additional Resources

- **Apache Turbine Documentation:** https://turbine.apache.org/
- **Spring Framework Documentation:** https://spring.io/projects/spring-framework
- **Torque ORM Documentation:** https://db.apache.org/torque/
- **Fulcrum Security Documentation:** https://fulcrum.apache.org/security/

## Support

For issues or questions:
1. Check application logs: `logs/` directory
2. Review Tomcat logs: `C:\applicationservers\apache-tomcat-10.1.44\logs\`
3. Verify database connectivity and data
4. Check REST API responses for error messages

## Version Information

- **Project Version:** 1.0-SNAPSHOT
- **Turbine Version:** 7.0
- **Spring Framework Version:** 6.x
- **Java Version:** 17+
- **MySQL Version:** 8.0+

---

**Last Updated:** January 2026

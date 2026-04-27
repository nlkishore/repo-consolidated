# UOB Turbine 7 Portal - Multi-Module Project Documentation

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Module Details](#module-details)
4. [Technology Stack](#technology-stack)
5. [Build System](#build-system)
6. [Deployment](#deployment)
7. [Configuration](#configuration)
8. [API Documentation](#api-documentation)
9. [Development Guide](#development-guide)
10. [Troubleshooting](#troubleshooting)

## Project Overview

**Project Name**: UOB Turbine 7 Portal Multi-Module  
**Version**: 1.0-SNAPSHOT  
**Type**: Multi-module Maven Project  
**Packaging**: WAR (Web Application Archive)  
**Java Version**: 17  
**Framework**: Apache Turbine 7.0 + Spring Framework 6.1.5

This is a standalone, portable multi-module Maven project that can be built and deployed from any machine or location without dependencies on external projects.

## Architecture

### Multi-Module Structure

The project is organized into four modules following a layered architecture:

```
┌─────────────────────────────────────────┐
│           webapp (WAR Module)           │
│  - Web resources, templates, config    │
│  - Deployment profiles (Tomcat/JBoss)  │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼──────┐  ┌──────▼──────────┐
│spring-rest- │  │turbine-model-   │
│api          │  │controller       │
│             │  │                 │
│- REST APIs  │  │- Turbine MVC    │
│- DTOs       │  │- Screens/Actions│
│- Spring     │  │- Services       │
│  Config     │  │- Flux Framework │
└──────┬──────┘  └──────┬──────────┘
       │                │
       └───────┬────────┘
               │
       ┌───────▼──────┐
       │  torque-orm  │
       │              │
       │- OM Classes  │
       │- Schema      │
       │- DB Access   │
       └──────────────┘
```

### Module Dependencies

- **torque-orm**: Base module, no internal dependencies
- **turbine-model-controller**: Depends on `torque-orm`
- **spring-rest-api**: Depends on `torque-orm` and `turbine-model-controller`
- **webapp**: Depends on all three modules

## Module Details

### 1. torque-orm

**Purpose**: Database access layer using Torque ORM

**Contents**:
- Torque schema definitions (`gtp-security-schema.xml`, etc.)
- Generated OM classes (Base classes in `target/generated-sources`)
- Custom OM classes (GtpUser, GtpRole, GtpPermission, etc.)
- Database access layer

**Key Classes**:
- `com.uob.om.GtpUser` - User entity
- `com.uob.om.GtpRole` - Role entity
- `com.uob.om.GtpGroup` - Group entity
- `com.uob.om.GtpPermission` - Permission entity

**Build Output**: JAR file

### 2. turbine-model-controller

**Purpose**: Turbine MVC framework components

**Contents**:
- Turbine screens (Velocity template handlers)
- Turbine actions (request handlers)
- Flux framework components
- Turbine services and utilities
- Wrapper classes

**Key Packages**:
- `com.uob.modules.screens` - Screen classes
- `com.uob.modules.actions` - Action classes
- `com.uob.services.pull` - Pull services
- `flux.modules.*` - Flux framework components

**Build Output**: JAR file

### 3. spring-rest-api

**Purpose**: Spring Framework REST API integration

**Contents**:
- REST controllers
- Data Transfer Objects (DTOs)
- Spring configuration
- Service layer bridging Turbine and Spring

**Key Classes**:
- `com.uob.controller.UserRestController` - User REST API
- `com.uob.controller.GroupRestController` - Group REST API
- `com.uob.controller.RoleRestController` - Role REST API
- `com.uob.controller.PermissionRestController` - Permission REST API
- `com.uob.service.TurbineSecurityService` - Security service
- `com.uob.config.SpringConfig` - Spring configuration

**REST Endpoints**:
- `GET /api/users` - List all users
- `GET /api/groups` - List all groups
- `GET /api/roles` - List all roles
- `GET /api/permissions` - List all permissions

**Build Output**: JAR file

### 4. webapp

**Purpose**: Web application packaging and deployment

**Contents**:
- Web resources (WEB-INF, templates, static files)
- SQL data files
- Docker resources
- Application server configurations

**Profiles**:
- **tomcat** (default): Generates WAR for Apache Tomcat
  - Uses `web.xml` for servlet configuration
  - Output: `uob-t7-portal-tomcat.war`
  
- **jboss8**: Generates WAR for JBoss 8 (WildFly)
  - Uses `web-jboss.xml` for servlet configuration
  - Includes JBoss-specific dependencies
  - Output: `uob-t7-portal-jboss8.war`

**Build Output**: WAR file

## Technology Stack

### Core Frameworks
- **Apache Turbine 7.0**: Web application framework
- **Spring Framework 6.1.5**: Dependency injection and REST support
- **Torque ORM 6.0**: Object-relational mapping
- **Fulcrum Security 4.0.0**: Security framework

### Web Technologies
- **Jakarta EE 10**: Servlet API 6.0
- **Velocity**: Template engine
- **Jackson 2.17.1**: JSON processing

### Database
- **MySQL 8.x**: Database server
- **MySQL Connector/J 9.1.0**: JDBC driver

### Build Tools
- **Maven 3.6+**: Build and dependency management
- **JaCoCo 0.8.12**: Code coverage
- **JUnit 5.10.2**: Testing framework

### Application Servers
- **Apache Tomcat 10.x**: Servlet container
- **JBoss 8 (WildFly)**: Application server

## Build System

### Maven Configuration

**Parent POM**: Manages common dependencies and versions

**Key Properties**:
- `maven.compile.source`: 17
- `maven.compile.target`: 17
- `spring.version`: 6.1.5
- `torque.version`: 6.0
- `turbine.core`: 7.0

### Build Phases

1. **generate-sources**: Torque generates OM classes
2. **process-sources**: Build helper adds generated sources
3. **compile**: Compiles all source files
4. **package**: Creates JAR/WAR files
5. **install**: Installs to local Maven repository

### Build Commands

```bash
# Full build
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl torque-orm -am

# Build WAR for Tomcat
mvn clean package -DskipTests -Ptomcat

# Build WAR for JBoss
mvn clean package -DskipTests -Pjboss8
```

## Deployment

### Prerequisites

1. Java 17 installed
2. MySQL 8.x running
3. Application server (Tomcat 10.x or JBoss 8) installed
4. Database created and configured

### Deployment Steps

#### Apache Tomcat

1. Build WAR:
```bash
mvn clean package -DskipTests -Ptomcat
```

2. Deploy:
```bash
cp webapp/target/uob-t7-portal-tomcat.war $CATALINA_HOME/webapps/
```

3. Start Tomcat:
```bash
$CATALINA_HOME/bin/startup.sh
```

4. Verify:
- Web UI: `http://localhost:8080/uob-t7-portal-tomcat/`
- REST API: `http://localhost:8080/uob-t7-portal-tomcat/api/users`

#### JBoss 8 (WildFly)

1. Build WAR:
```bash
mvn clean package -DskipTests -Pjboss8
```

2. Deploy:
```bash
cp webapp/target/uob-t7-portal-jboss8.war $JBOSS_HOME/standalone/deployments/
```

3. Start JBoss:
```bash
$JBOSS_HOME/bin/standalone.sh
```

4. Verify:
- Web UI: `http://localhost:8080/uob-t7-portal-jboss8/`
- REST API: `http://localhost:8080/uob-t7-portal-jboss8/api/users`

## Configuration

### Database Configuration

File: `webapp/src/main/webapp/WEB-INF/conf/Torque.properties`

```properties
torque.database.default=kishore
torque.database.kishore.adapter=mysql
torque.database.kishore.url=jdbc:mysql://localhost:3306/kishore?allowPublicKeyRetrieval=true
torque.database.kishore.user=kishore
torque.database.kishore.password=Kish1381@
```

### Turbine Configuration

File: `webapp/src/main/webapp/WEB-INF/conf/componentConfiguration.xml`

Configures security managers to use GTP tables:
- `GtpUser` for user management
- `GtpGroup` for group management
- `GtpRole` for role management
- `GtpPermission` for permission management

### Spring Configuration

File: `spring-rest-api/src/main/java/com/uob/config/SpringConfig.java`

- Component scanning: `com.uob`
- Web MVC enabled
- Jackson JSON support

## API Documentation

### REST Endpoints

All endpoints are prefixed with `/api`

#### Users
- `GET /api/users` - Get all users
  - Response: `List<UserDTO>`

#### Groups
- `GET /api/groups` - Get all groups
  - Response: `List<GroupDTO>`

#### Roles
- `GET /api/roles` - Get all roles
  - Response: `List<RoleDTO>`

#### Permissions
- `GET /api/permissions` - Get all permissions
  - Response: `List<PermissionDTO>`

### Swagger UI

Access API documentation at:
```
http://localhost:8080/{context}/swagger-ui.html
```

## Development Guide

### Setting Up Development Environment

1. Clone or extract project to any location
2. Ensure Java 17 and Maven are installed
3. Configure database connection
4. Build project:
```bash
mvn clean install -DskipTests
```

### Adding New Features

1. **New Database Table**: Add schema to `torque-orm/src/main/torque-schema/`
2. **New REST Endpoint**: Add controller to `spring-rest-api/src/main/java/com/uob/controller/`
3. **New Turbine Screen**: Add to `turbine-model-controller/src/main/java/com/uob/modules/screens/`
4. **New Service**: Add to appropriate module

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Follow existing code structure

### Testing

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Troubleshooting

### Common Issues

#### 1. Base Classes Not Found

**Error**: `cannot find symbol: class BaseGtpUser`

**Solution**:
```bash
mvn clean generate-sources -pl torque-orm
mvn clean install -DskipTests
```

#### 2. Database Connection Failed

**Error**: `java.sql.SQLException: Access denied`

**Solution**:
- Verify MySQL is running
- Check credentials in `Torque.properties`
- Ensure database exists
- For MySQL 8.x, add `allowPublicKeyRetrieval=true` to JDBC URL

#### 3. Module Not Found

**Error**: `Could not resolve dependencies`

**Solution**:
```bash
mvn clean install -DskipTests
```

This installs all modules to local Maven repository.

#### 4. WAR Deployment Failed

**Error**: `Deployment failed`

**Solution**:
- Check application server logs
- Verify WAR file is complete
- Check database connectivity
- Verify all dependencies are included

### Log Files

- **Tomcat**: `$CATALINA_HOME/logs/catalina.out`
- **JBoss**: `$JBOSS_HOME/standalone/log/server.log`
- **Application**: `webapp/logs/application.log`

## Project Portability

This project is **completely portable**:

✅ No hardcoded paths  
✅ No dependencies on external projects  
✅ All dependencies managed by Maven  
✅ Relative paths only  
✅ Can be cloned/moved to any location  
✅ Can be built on any machine with Java/Maven  

## Support and Maintenance

### Version Information

- **Project Version**: 1.0-SNAPSHOT
- **Java**: 17
- **Maven**: 3.6+
- **Turbine**: 7.0
- **Spring**: 6.1.5

### Future Enhancements

- Additional REST endpoints (POST, PUT, DELETE)
- Authentication and authorization
- API versioning
- Integration tests
- Docker containerization

## License

Licensed under the Apache License, Version 2.0

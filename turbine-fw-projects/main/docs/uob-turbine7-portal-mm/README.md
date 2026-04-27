# UOB Turbine 7 Portal - Multi-Module Project

A standalone, portable multi-module Maven project for the UOB Turbine 7 Portal application.

## Quick Start

### One-Time Setup

**IMPORTANT**: Base classes must be copied/committed before first build:

```powershell
# Copy base classes from original project
cd torque-orm
New-Item -ItemType Directory -Path "target\generated-sources\com\uob\om" -Force
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" -Destination "target\generated-sources\com\uob\om\" -Force
cd ..

# Commit to version control (RECOMMENDED)
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

### Regular Builds

After setup, use standard Maven commands:

```bash
# Build all modules
mvn clean install -DskipTests

# Build WAR for Tomcat
mvn package -DskipTests -Ptomcat

# Build WAR for JBoss 8
mvn package -DskipTests -Pjboss8
```

## Project Structure

- **torque-orm**: Database access layer (Torque ORM)
- **turbine-model-controller**: Turbine MVC components
- **spring-rest-api**: Spring REST API integration
- **webapp**: Web application with deployment profiles

## Why Base Classes?

Torque ORM generates base classes that are:
- **Stable** - Only change when schema changes
- **Required** - Needed for compilation
- **Best Practice** - Should be committed to version control

This is standard practice (Spring Boot, Protocol Buffers, gRPC all commit generated code).

## Build Workflow

### ❌ Multiple Steps (Before Setup):
```bash
# Manual copy, avoid clean, etc.
```

### ✅ Single Command (After Setup):
```bash
mvn clean install -DskipTests
```

## Documentation

- **[SECURITY-AUDIT.md](SECURITY-AUDIT.md)** - Security audit, CVE assessment, and customized classes review
- **[DEVELOPMENT-PROMPTS.md](DEVELOPMENT-PROMPTS.md)** - Complete history of developer prompts and requests
- **[FINAL-BUILD-SOLUTION.md](FINAL-BUILD-SOLUTION.md)** - Complete build solution
- **[BUILD-SOLUTION.md](BUILD-SOLUTION.md)** - Build workflow guide
- **[BUILD-INSTRUCTIONS.md](BUILD-INSTRUCTIONS.md)** - Detailed instructions
- **[PROJECT-DOCUMENTATION.md](PROJECT-DOCUMENTATION.md)** - Technical documentation
- **[SETUP-GUIDE.md](SETUP-GUIDE.md)** - Step-by-step setup
- **[TORQUE-BASE-CLASSES.md](TORQUE-BASE-CLASSES.md)** - Base class guide

## Features

- ✅ Standalone and portable (no external project dependencies)
- ✅ Multi-module Maven architecture
- ✅ Support for Apache Tomcat and JBoss 8
- ✅ REST API with Spring Framework
- ✅ Turbine MVC framework
- ✅ Torque ORM for database access
- ✅ Standard Maven workflow (after one-time setup)

## Requirements

- Java 17+
- Maven 3.6+
- MySQL 8.x
- Apache Tomcat 10.x or JBoss 8 (WildFly)

## Deployment and Access URLs

### Deployment

After building the WAR file, deploy it to your application server:

**Tomcat:**
```bash
copy webapp\target\uob-t7-portal-mm-tomcat.war C:\applicationservers\apache-tomcat-10.1.44\webapps\
```

**JBoss 8:**
```bash
copy webapp\target\uob-portal-mm-jboss.war <JBOSS_HOME>\standalone\deployments\
```

### Base URLs

**Tomcat Deployment:**
- Base URL: `http://localhost:8081/uob-t7-portal-mm-tomcat`
- Context Path: `/uob-t7-portal-mm-tomcat`

**JBoss 8 Deployment:**
- Base URL: `http://localhost:8080/uob-portal-mm-jboss`
- Context Path: `/uob-portal-mm-jboss`

### Web Application Pages (Turbine MVC)

**Home/Index Page:**
- URL: `http://localhost:8081/uob-t7-portal-mm-tomcat/app`
- Description: Main application entry point

**Login Page:**
- URL: `http://localhost:8081/uob-t7-portal-mm-tomcat/app?action=LoginUser`
- Description: User login page

**User Profile:**
- URL: `http://localhost:8081/uob-t7-portal-mm-tomcat/app?screen=UserProfile`
- Description: View user profile information

**Logout:**
- URL: `http://localhost:8081/uob-t7-portal-mm-tomcat/app?action=LogoutUser`
- Description: Logout user session

### REST API Endpoints

All REST API endpoints are prefixed with `/api` and return JSON responses.

#### User Endpoints

**Get All Users**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/users`
- **Description:** Retrieve all users in the system
- **Response:** Array of UserDTO objects
- **Example:**
  ```json
  [
    {
      "userId": 1,
      "loginName": "admin",
      "firstName": "Admin",
      "lastName": "User",
      "email": "admin@example.com",
      "confirmed": true,
      "lastLogin": "2026-01-24T10:00:00",
      "created": "2026-01-01T00:00:00",
      "modified": "2026-01-24T10:00:00"
    }
  ]
  ```

**Get User by Login Name**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/users/{loginName}`
- **Description:** Retrieve a specific user by login name
- **Parameters:**
  - `loginName` (path parameter): The user's login name
- **Response:** UserDTO object
- **Example:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/users/admin`

#### Group Endpoints

**Get All Groups**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/groups`
- **Description:** Retrieve all groups in the system
- **Response:** Array of GroupDTO objects
- **Example:**
  ```json
  [
    {
      "groupId": 1,
      "groupName": "Administrators"
    },
    {
      "groupId": 2,
      "groupName": "Users"
    }
  ]
  ```

**Get Group by Name**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/groups/{groupName}`
- **Description:** Retrieve a specific group by name
- **Parameters:**
  - `groupName` (path parameter): The group name
- **Response:** GroupDTO object
- **Example:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/groups/Administrators`

#### Role Endpoints

**Get All Roles**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/roles`
- **Description:** Retrieve all roles in the system
- **Response:** Array of RoleDTO objects
- **Example:**
  ```json
  [
    {
      "roleId": 1,
      "roleName": "admin"
    },
    {
      "roleId": 2,
      "roleName": "user"
    }
  ]
  ```

**Get Role by Name**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/roles/{roleName}`
- **Description:** Retrieve a specific role by name
- **Parameters:**
  - `roleName` (path parameter): The role name
- **Response:** RoleDTO object
- **Example:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/roles/admin`

**Get Permissions for a Role**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/roles/{roleName}/permissions`
- **Description:** Retrieve all permissions assigned to a specific role
- **Parameters:**
  - `roleName` (path parameter): The role name
- **Response:** Array of permission names (strings)
- **Example:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/roles/admin/permissions`
- **Response Example:**
  ```json
  [
    "read",
    "write",
    "delete"
  ]
  ```

#### Permission Endpoints

**Get All Permissions**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/permissions`
- **Description:** Retrieve all permissions in the system
- **Response:** Array of PermissionDTO objects
- **Example:**
  ```json
  [
    {
      "permissionId": 1,
      "permissionName": "read"
    },
    {
      "permissionId": 2,
      "permissionName": "write"
    }
  ]
  ```

**Get Permission by Name**
- **Method:** `GET`
- **URL:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/permissions/{permissionName}`
- **Description:** Retrieve a specific permission by name
- **Parameters:**
  - `permissionName` (path parameter): The permission name
- **Response:** PermissionDTO object
- **Example:** `http://localhost:8081/uob-t7-portal-mm-tomcat/api/permissions/read`

### API Testing

You can test the REST API endpoints using:

**cURL:**
```bash
# Get all users
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/users

# Get specific user
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/users/admin

# Get all groups
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/groups

# Get all roles
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/roles

# Get all permissions
curl http://localhost:8081/uob-t7-portal-mm-tomcat/api/permissions
```

**Browser:**
Simply navigate to the endpoint URLs in your browser to view JSON responses.

**Postman/API Client:**
Import the endpoints above for testing with your preferred API client.

### Response Format

All REST API endpoints return JSON responses with the following structure:

**Success Response:**
- Status Code: `200 OK`
- Content-Type: `application/json`
- Body: JSON object or array

**Error Responses:**
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error occurred

## License

Licensed under the Apache License, Version 2.0

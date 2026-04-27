# Turbine Web Application

This is a Turbine 7.0 web application generated from the Apache Turbine archetype.

## Project Information

- **Group ID**: com.example
- **Artifact ID**: turbine-webapp-new
- **Version**: 1.0-SNAPSHOT
- **Packaging**: WAR
- **Java Version**: 17
- **Turbine Version**: 7.0
- **Torque Version**: 6.0

## Project Structure

```
turbine-webapp-new/
├── src/
│   ├── main/
│   │   ├── java/              # Java source files
│   │   │   ├── modules/       # Turbine modules (actions, screens)
│   │   │   ├── om/            # Object Model (Torque generated)
│   │   │   ├── services/      # Turbine services
│   │   │   └── wrapper/       # Wrapper classes
│   │   ├── torque-schema/     # Torque database schema definitions
│   │   ├── webapp/            # Web application resources
│   │   │   ├── WEB-INF/       # Web configuration
│   │   │   ├── templates/     # Velocity templates
│   │   │   └── resources/     # Static resources
│   │   └── data/              # Sample data SQL files
│   └── test/
│       ├── java/              # Test source files
│       └── resources/         # Test resources
└── pom.xml                    # Maven project file
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0+ or MariaDB 10.5+ (for database)

## Database Configuration

The application uses Torque ORM for database access. Configure your database connection in:

- `src/main/webapp/WEB-INF/conf/Torque.properties`
- Or use JNDI configuration

Default database settings:
- Database: `turbine`
- Driver: `com.mysql.cj.jdbc.Driver`
- URL: `jdbc:mysql://localhost:3306/turbine`

## Building the Project

### Compile
```bash
mvn clean compile
```

### Generate Torque Object Model
```bash
mvn generate-sources
```

### Package WAR
```bash
mvn clean package
```

### Run with Jetty (Embedded)
```bash
mvn jetty:run
```

The application will be available at: `http://localhost:8081`

## Maven Profiles

### MySQL Profile (Default)
```bash
mvn clean package -Pmysql
```

### MariaDB Profile
```bash
mvn clean package -Pmariadb
```

### Docker Profile
```bash
mvn clean package -Pdocker
```

## Deployment

### Tomcat 10.x
1. Build the WAR: `mvn clean package`
2. Copy `target/turbine-webapp-new-1.0-SNAPSHOT.war` to Tomcat's `webapps/` directory
3. Start Tomcat

### JBoss 8 / WildFly 31+
1. Build the WAR: `mvn clean package`
2. Copy the WAR to `standalone/deployments/`
3. Start JBoss

## Configuration Files

- **web.xml**: `src/main/webapp/WEB-INF/web.xml` - Servlet configuration
- **Torque.properties**: `src/main/webapp/WEB-INF/conf/Torque.properties` - Database configuration
- **TurbineResources.properties**: `src/main/webapp/WEB-INF/conf/TurbineResources.properties` - Turbine configuration

## Key Features

- Turbine 7.0 MVC framework
- Torque 6.0 ORM
- Velocity template engine
- Fulcrum security framework
- Jakarta EE 10 compatible
- Log4j2 logging

## Next Steps

1. Configure database connection in `Torque.properties`
2. Run `mvn generate-sources` to generate Torque object model
3. Create database schema using generated SQL files
4. Customize Velocity templates in `src/main/webapp/templates/`
5. Add your business logic in `src/main/java/modules/`

## Documentation

- [Apache Turbine Documentation](https://turbine.apache.org/)
- [Torque ORM Documentation](https://db.apache.org/torque/)
- [Velocity Template Engine](https://velocity.apache.org/)

## License

Licensed under the Apache License, Version 2.0

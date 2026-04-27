# Quick Start Guide

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.x (for database)

## 1. Build the Project

```bash
cd uob-turbine7-portal-mm
mvn clean install -DskipTests
```

## 2. Setup Database

```sql
CREATE DATABASE kishore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'kishore'@'localhost' IDENTIFIED BY 'Kish1381@';
GRANT ALL PRIVILEGES ON kishore.* TO 'kishore'@'localhost';
FLUSH PRIVILEGES;
```

Execute schema:
```bash
mysql -u kishore -p kishore < torque-orm/target/generated-sql/torque/mysql/gtp-security-schema.sql
```

## 3. Build WAR File

### For Tomcat:
```bash
mvn clean package -DskipTests -Ptomcat
```

### For JBoss 8:
```bash
mvn clean package -DskipTests -Pjboss8
```

## 4. Deploy

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

## 5. Access Application

- **Web UI**: `http://localhost:8080/{context}/`
- **REST API**: `http://localhost:8080/{context}/api/users`
- **Swagger**: `http://localhost:8080/{context}/swagger-ui.html`

## Troubleshooting

### Base Classes Not Found
```bash
mvn clean generate-sources -pl torque-orm
mvn clean install -DskipTests
```

### Database Connection Issues
Check `webapp/src/main/webapp/WEB-INF/conf/Torque.properties`

## Documentation

- **[BUILD-INSTRUCTIONS.md](BUILD-INSTRUCTIONS.md)** - Detailed build instructions
- **[PROJECT-DOCUMENTATION.md](PROJECT-DOCUMENTATION.md)** - Complete documentation
- **[INDEPENDENCE-VERIFICATION.md](INDEPENDENCE-VERIFICATION.md)** - Portability verification

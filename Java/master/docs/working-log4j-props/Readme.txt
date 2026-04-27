Q: Exaplain the Key Significance of Log4j2 properties - status , name ,property,rootLogger

The `log4j2.properties` file is a configuration file used by Log4j2 to specify various settings related to logging behavior. Here are the key significance of some of the commonly used properties in the `log4j2.properties` file:

1. **status**:
   - The `status` property specifies the Log4j2 internal status level. 
   - It determines the status level of the internal Log4j2 logging itself. 
   - Possible values are `trace`, `debug`, `info`, `warn`, `error`, and `fatal`.
   - This property is useful for debugging Log4j2's own configuration and initialization process.

2. **name**:
   - The `name` property specifies the name of the LoggerContext.
   - The LoggerContext is the central component of Log4j2's logging system.
   - It is responsible for managing loggers, appenders, filters, and other logging components.
   - By default, the name is set to "DefaultContext". You can customize it if needed.

3. **property**:
   - The `property` property defines custom properties that can be used in the configuration.
   - It allows you to define variables that can be referenced in other parts of the configuration.
   - For example, you can define a property like `property.filename = logs/app.log`, and then refer to it as `${filename}` elsewhere in the configuration.

4. **rootLogger**:
   - The `rootLogger` property configures the root logger, which is the parent logger of all loggers in the system.
   - It defines the logging level and appenders for all loggers that do not have a specific configuration.
   - The root logger typically captures all log messages unless they are explicitly handled by more specific loggers.
   - The syntax for defining the rootLogger is `<rootLogger level="level" includeLocation="true/false">...</rootLogger>`.
   - `level` specifies the logging level for the root logger (e.g., `DEBUG`, `INFO`, `ERROR`, etc.).
   - `includeLocation` specifies whether to include location information (such as file name and line number) in log messages.

These are some of the key properties in the `log4j2.properties` file that are commonly used to configure Log4j2. Understanding these properties is essential for configuring Log4j2 to meet the logging requirements of your application.

Q: Share one example to have specific Logger instead of rootLogger to udnerstand more customization

Ans: Certainly! Let's consider an example where we configure a specific logger instead of the root logger in the `log4j2.properties` file. In this example, we'll configure a custom logger for a specific class and define its logging level and appender.

Assume we have a class `MyClass` and we want to configure logging for it. Here's how you can do it:

1. **Create a `log4j2.properties` file**:
   Create a file named `log4j2.properties` in your project's `src/main/resources` directory (for Maven projects). Add the following configuration:

   ```properties
   status = error
   name = PropertiesConfig

   # Define custom properties (optional)
   property.filename = logs/app.log

   # Define an appender
   appenders = console, file

   appender.console.type = Console
   appender.console.name = STDOUT
   appender.console.layout.type = PatternLayout
   appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n

   appender.file.type = File
   appender.file.name = FILE
   appender.file.fileName = ${filename}
   appender.file.layout.type = PatternLayout
   appender.file.layout.pattern = %d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n

   # Define a custom logger for MyClass
   logger.MyClass.name = com.example.MyClass
   logger.MyClass.level = debug
   logger.MyClass.appenderRef.file.ref = FILE

   # Root logger settings (optional)
   rootLogger.level = error
   rootLogger.appenderRefs = stdout
   rootLogger.appenderRef.stdout.ref = STDOUT
   ```

2. **Define Logging in `MyClass`**:
   In your Java class `MyClass`, define a logger using Log4j2:

   ```java
   package com.example;

   import org.apache.logging.log4j.LogManager;
   import org.apache.logging.log4j.Logger;

   public class MyClass {
       private static final Logger logger = LogManager.getLogger(MyClass.class);

       public static void main(String[] args) {
           // Log messages
           logger.debug("Debug message");
           logger.info("Info message");
           logger.warn("Warning message");
           logger.error("Error message");
       }
   }
   ```

In this example:

- We configure a custom logger for the class `com.example.MyClass` in the `log4j2.properties` file.
- The logger is configured to log messages at the `DEBUG` level and above.
- The logger uses the `FILE` appender to log messages to the specified file (`logs/app.log`).
- In `MyClass`, we use the configured logger to log messages at different levels (`DEBUG`, `INFO`, `WARN`, `ERROR`).

This example demonstrates how to customize logging for a specific class using Log4j2 configuration properties. Adjust the configuration according to your specific logging requirements.

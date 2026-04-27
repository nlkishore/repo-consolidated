mvn archetype:generate -DgroupId=com.example -DartifactId=sonarlint-project -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

<dependency>
    <groupId>org.sonarsource.sonarlint.core</groupId>
    <artifactId>sonarlint-core</artifactId>
    <version>3.3.0.1492</version>
</dependency>


mvn compile exec:java -Dexec.mainClass="com.example.SonarLintRunner"

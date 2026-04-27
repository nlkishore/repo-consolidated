package com.example;

import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngineFactory;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintProject;

import java.nio.file.Paths;

public class SonarLintRunner1 {
    public static void main(String[] args) {
        StandaloneGlobalConfiguration globalConfig = StandaloneGlobalConfiguration.builder()
                .addPlugin(Paths.get("path/to/sonar-java-plugin.jar"))
                .setWorkDir(Paths.get("path/to/workdir"))
                .build();

        try (StandaloneSonarLintEngine engine = StandaloneSonarLintEngineFactory.create(globalConfig)) {
            StandaloneSonarLintProject project = engine.createStandaloneProject(
                    Paths.get("path/to/project"),
                    "project-key");

            AnalysisResults results = engine.analyze(project);

            for (Issue issue : results.getIssues()) {
                System.out.println(issue);
            }
        }
    }
}

package com.example;

import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintProject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class SonarLintRunner {
    public static void main(String[] args) {
        Path projectPath = Paths.get("C:/ApplicationChanges/PortalWorkSpace/Validator-Workspace/Sonar-Lint-CLI"); // Update this path

        try (StandaloneSonarLintEngine engine = StandaloneSonarLintEngine.builder().build()) {
            StandaloneSonarLintProject project = engine.createStandaloneProject(projectPath, Collections.emptyList());

            AnalysisResults results = engine.analyze(project, Collections.emptyList());

            for (Issue issue : results.issues()) {
                System.out.println("Issue found: " + issue.message());
                System.out.println("Severity: " + issue.severity());
                System.out.println("File: " + issue.inputFile().absolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

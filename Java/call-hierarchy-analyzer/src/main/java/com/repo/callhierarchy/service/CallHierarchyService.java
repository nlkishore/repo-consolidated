package com.repo.callhierarchy.service;

import com.repo.callhierarchy.config.AnalyzerConfig;
import com.repo.callhierarchy.graph.CallGraphBuilder;
import com.repo.callhierarchy.index.ProjectIndex;
import com.repo.callhierarchy.model.HierarchyReport;
import com.repo.callhierarchy.resolve.EntryResolver;
import java.io.IOException;
import java.time.Instant;
import java.util.logging.Logger;

public final class CallHierarchyService {
  private static final Logger LOG = Logger.getLogger(CallHierarchyService.class.getName());

  private final AnalyzerConfig config;

  public CallHierarchyService(AnalyzerConfig config) {
    this.config = config;
  }

  public HierarchyReport analyze(String entrySpec) throws IOException {
    long start = System.currentTimeMillis();
    ProjectIndex index = ProjectIndex.build(config);
    LOG.info(
        () ->
            "Indexed sources; jarsLoaded="
                + index.classpath().loadedJars().size()
                + " auditEntries="
                + index.classpathAudit().size());

    if (config.strictClasspath()) {
      boolean missingJar =
          index.classpathAudit().stream()
              .anyMatch(e -> e.kind().name().equals("JAR") && !e.readable());
      if (missingJar) {
        throw new IOException("Strict classpath: one or more JAR paths are not readable");
      }
    }

    EntryResolver.ResolvedEntry entry = new EntryResolver(index).resolve(entrySpec);
    CallGraphBuilder.BuildResult built =
        new CallGraphBuilder(index).build(entry.methodRef(), entry.declaration());

    HierarchyReport report =
        new HierarchyReport(
            entry.methodRef(),
            built.classes(),
            built.edges(),
            built.root(),
            built.unresolved(),
            built.cycles(),
            index.classpathAudit(),
            config.maxDepth(),
            Instant.now());

    LOG.info(
        () ->
            "Analysis done in "
                + (System.currentTimeMillis() - start)
                + "ms classes="
                + report.classes().size()
                + " edges="
                + report.edges().size()
                + " unresolved="
                + report.unresolved().size());
    return report;
  }
}

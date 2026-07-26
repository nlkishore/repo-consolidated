package com.repo.callhierarchy.export;

import com.repo.callhierarchy.model.CallEdge;
import com.repo.callhierarchy.model.HierarchyReport;
import com.repo.callhierarchy.model.Origin;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MermaidExporter {

  public void write(HierarchyReport report, Path outFile) throws IOException {
    Path parent = outFile.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    StringBuilder sb = new StringBuilder();
    sb.append("```mermaid\n");
    sb.append("flowchart TD\n");
    Set<String> nodes = new LinkedHashSet<>();
    nodes.add(report.entry().key());
    for (CallEdge e : report.edges()) {
      nodes.add(e.from().key());
      nodes.add(e.to().key());
    }
    int i = 0;
    java.util.Map<String, String> ids = new java.util.LinkedHashMap<>();
    for (String key : nodes) {
      String id = "N" + (i++);
      ids.put(key, id);
      String label = key;
      if (label.length() > 80) {
        label = label.substring(0, 77) + "...";
      }
      boolean jar =
          report.classes().stream()
              .anyMatch(
                  c ->
                      key.startsWith(c.typeFqn() + "#")
                          && c.origin() == Origin.JAR);
      if (jar) {
        sb.append("  ")
            .append(id)
            .append("[\"")
            .append(escape(label))
            .append(" (JAR)\"]\n");
      } else {
        sb.append("  ").append(id).append("[\"").append(escape(label)).append("\"]\n");
      }
    }
    for (CallEdge e : report.edges()) {
      sb.append("  ")
          .append(ids.get(e.from().key()))
          .append(" --> ")
          .append(ids.get(e.to().key()))
          .append("\n");
    }
    sb.append("```\n");
    Files.writeString(outFile, sb.toString(), StandardCharsets.UTF_8);
  }

  private static String escape(String s) {
    return s.replace("\"", "'");
  }
}

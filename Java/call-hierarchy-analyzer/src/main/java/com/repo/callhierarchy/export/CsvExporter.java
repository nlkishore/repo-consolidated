package com.repo.callhierarchy.export;

import com.repo.callhierarchy.model.CallEdge;
import com.repo.callhierarchy.model.CallNode;
import com.repo.callhierarchy.model.ClassRef;
import com.repo.callhierarchy.model.ClasspathEntry;
import com.repo.callhierarchy.model.HierarchyReport;
import com.repo.callhierarchy.model.UnresolvedCall;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvExporter {

  public void write(HierarchyReport report, Path outBase) throws IOException {
    Path parent = outBase.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    String base = outBase.getFileName().toString();
    Path dir = parent == null ? Path.of(".") : parent;

    writeSummary(report, dir.resolve(base + "-summary.csv"));
    writeClasses(report, dir.resolve(base + "-classes.csv"));
    writeHierarchy(report, dir.resolve(base + "-hierarchy.csv"));
    writeEdges(report, dir.resolve(base + "-edges.csv"));
    writeUnresolved(report, dir.resolve(base + "-unresolved.csv"));
    writeClasspath(report, dir.resolve(base + "-classpath.csv"));
  }

  private void writeSummary(HierarchyReport report, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("metric,value");
    lines.add(row("entry", report.entry().key()));
    lines.add(row("class_count", String.valueOf(report.classes().size())));
    lines.add(row("jar_class_count", String.valueOf(report.jarClassCount())));
    lines.add(row("edge_count", String.valueOf(report.edges().size())));
    lines.add(row("unresolved_count", String.valueOf(report.unresolved().size())));
    lines.add(row("max_depth_limit", String.valueOf(report.depthLimit())));
    lines.add(row("generated_at", report.generatedAt().toString()));
    Files.write(file, lines, StandardCharsets.UTF_8);
  }

  private void writeClasses(HierarchyReport report, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("class_fqn,simple_name,package,origin,jar_name,first_depth,role_hint");
    for (ClassRef c : report.classes()) {
      lines.add(
          row(
              c.typeFqn(),
              c.simpleName(),
              c.packageName(),
              c.origin().name(),
              nullToEmpty(c.jarName()),
              String.valueOf(c.firstDepth()),
              nullToEmpty(c.roleHint())));
    }
    Files.write(file, lines, StandardCharsets.UTF_8);
  }

  private void writeHierarchy(HierarchyReport report, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add(
        "depth,indent_label,class_fqn,method,origin,jar_name,parent_method,path,leaf_reason");
    flatten(report.root(), null, lines);
    Files.write(file, lines, StandardCharsets.UTF_8);
  }

  private void flatten(CallNode node, String parentKey, List<String> lines) {
    String indent = "·".repeat(Math.max(0, node.depth())) + node.method().shortLabel();
    lines.add(
        row(
            String.valueOf(node.depth()),
            indent,
            node.method().typeFqn(),
            node.method().methodName(),
            node.method().origin().name(),
            nullToEmpty(node.method().jarName()),
            nullToEmpty(parentKey),
            nullToEmpty(node.path()),
            node.leafReason().name()));
    for (CallNode child : node.children()) {
      flatten(child, node.method().key(), lines);
    }
  }

  private void writeEdges(HierarchyReport report, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add(
        "caller_class,caller_method,callee_class,callee_method,callee_origin,call_site_line,depth,kind");
    for (CallEdge e : report.edges()) {
      lines.add(
          row(
              e.from().typeFqn(),
              e.from().methodName(),
              e.to().typeFqn(),
              e.to().methodName(),
              e.to().origin().name(),
              e.callSiteLine() == null ? "" : String.valueOf(e.callSiteLine()),
              String.valueOf(e.depth()),
              e.kind().name()));
    }
    Files.write(file, lines, StandardCharsets.UTF_8);
  }

  private void writeUnresolved(HierarchyReport report, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("caller,call_text,line,reason,suggested_fix");
    for (UnresolvedCall u : report.unresolved()) {
      lines.add(
          row(
              u.callerKey(),
              u.callText(),
              u.line() == null ? "" : String.valueOf(u.line()),
              u.reason(),
              u.suggestedFix()));
    }
    Files.write(file, lines, StandardCharsets.UTF_8);
  }

  private void writeClasspath(HierarchyReport report, Path file) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("kind,path,readable");
    for (ClasspathEntry e : report.classpathAudit()) {
      lines.add(row(e.kind().name(), e.path().toString(), e.readable() ? "Y" : "N"));
    }
    Files.write(file, lines, StandardCharsets.UTF_8);
  }

  private static String row(String... cols) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cols.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(escape(cols[i]));
    }
    return sb.toString();
  }

  private static String escape(String v) {
    if (v == null) {
      return "";
    }
    boolean needQuotes = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
    String escaped = v.replace("\"", "\"\"");
    return needQuotes ? "\"" + escaped + "\"" : escaped;
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }
}

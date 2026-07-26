package com.repo.callhierarchy.export;

import com.repo.callhierarchy.model.CallEdge;
import com.repo.callhierarchy.model.CallNode;
import com.repo.callhierarchy.model.ClassRef;
import com.repo.callhierarchy.model.ClasspathEntry;
import com.repo.callhierarchy.model.HierarchyReport;
import com.repo.callhierarchy.model.UnresolvedCall;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelExporter {

  public void write(HierarchyReport report, Path outFile) throws IOException {
    Path parent = outFile.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      writeSummary(wb, report);
      writeClasses(wb, report);
      writeHierarchy(wb, report);
      writeEdges(wb, report);
      writeUnresolved(wb, report);
      writeClasspath(wb, report);
      try (OutputStream os = Files.newOutputStream(outFile)) {
        wb.write(os);
      }
    }
  }

  private void writeSummary(XSSFWorkbook wb, HierarchyReport report) {
    Sheet sheet = wb.createSheet("Summary");
    int r = 0;
    r = kv(sheet, r, "entry", report.entry().key());
    r = kv(sheet, r, "class_count", String.valueOf(report.classes().size()));
    r = kv(sheet, r, "jar_class_count", String.valueOf(report.jarClassCount()));
    r = kv(sheet, r, "edge_count", String.valueOf(report.edges().size()));
    r = kv(sheet, r, "unresolved_count", String.valueOf(report.unresolved().size()));
    r = kv(sheet, r, "max_depth_limit", String.valueOf(report.depthLimit()));
    kv(sheet, r, "generated_at", report.generatedAt().toString());
  }

  private void writeClasses(XSSFWorkbook wb, HierarchyReport report) {
    Sheet sheet = wb.createSheet("Classes");
    header(
        sheet,
        "class_fqn",
        "simple_name",
        "package",
        "origin",
        "jar_name",
        "first_depth",
        "role_hint");
    int r = 1;
    for (ClassRef c : report.classes()) {
      Row row = sheet.createRow(r++);
      int cidx = 0;
      row.createCell(cidx++).setCellValue(c.typeFqn());
      row.createCell(cidx++).setCellValue(c.simpleName());
      row.createCell(cidx++).setCellValue(c.packageName());
      row.createCell(cidx++).setCellValue(c.origin().name());
      row.createCell(cidx++).setCellValue(c.jarName() == null ? "" : c.jarName());
      row.createCell(cidx++).setCellValue(c.firstDepth());
      row.createCell(cidx).setCellValue(c.roleHint() == null ? "" : c.roleHint());
    }
  }

  private void writeHierarchy(XSSFWorkbook wb, HierarchyReport report) {
    Sheet sheet = wb.createSheet("Hierarchy");
    header(
        sheet,
        "depth",
        "indent_label",
        "class_fqn",
        "method",
        "origin",
        "jar_name",
        "parent_method",
        "path",
        "leaf_reason");
    ListBuffer buf = new ListBuffer();
    flatten(report.root(), null, buf);
    int r = 1;
    for (String[] cols : buf.rows) {
      Row row = sheet.createRow(r++);
      for (int i = 0; i < cols.length; i++) {
        row.createCell(i).setCellValue(cols[i]);
      }
    }
  }

  private void flatten(CallNode node, String parentKey, ListBuffer buf) {
    String indent = "  ".repeat(Math.max(0, node.depth())) + node.method().shortLabel();
    buf.rows.add(
        new String[] {
          String.valueOf(node.depth()),
          indent,
          node.method().typeFqn(),
          node.method().methodName(),
          node.method().origin().name(),
          node.method().jarName() == null ? "" : node.method().jarName(),
          parentKey == null ? "" : parentKey,
          node.path() == null ? "" : node.path(),
          node.leafReason().name()
        });
    for (CallNode child : node.children()) {
      flatten(child, node.method().key(), buf);
    }
  }

  private void writeEdges(XSSFWorkbook wb, HierarchyReport report) {
    Sheet sheet = wb.createSheet("Edges");
    header(
        sheet,
        "caller_class",
        "caller_method",
        "callee_class",
        "callee_method",
        "callee_origin",
        "call_site_line",
        "depth",
        "kind");
    int r = 1;
    for (CallEdge e : report.edges()) {
      Row row = sheet.createRow(r++);
      int c = 0;
      row.createCell(c++).setCellValue(e.from().typeFqn());
      row.createCell(c++).setCellValue(e.from().methodName());
      row.createCell(c++).setCellValue(e.to().typeFqn());
      row.createCell(c++).setCellValue(e.to().methodName());
      row.createCell(c++).setCellValue(e.to().origin().name());
      row.createCell(c++).setCellValue(e.callSiteLine() == null ? "" : String.valueOf(e.callSiteLine()));
      row.createCell(c++).setCellValue(e.depth());
      row.createCell(c).setCellValue(e.kind().name());
    }
  }

  private void writeUnresolved(XSSFWorkbook wb, HierarchyReport report) {
    Sheet sheet = wb.createSheet("Unresolved");
    header(sheet, "caller", "call_text", "line", "reason", "suggested_fix");
    int r = 1;
    for (UnresolvedCall u : report.unresolved()) {
      Row row = sheet.createRow(r++);
      row.createCell(0).setCellValue(u.callerKey());
      row.createCell(1).setCellValue(u.callText());
      row.createCell(2).setCellValue(u.line() == null ? "" : String.valueOf(u.line()));
      row.createCell(3).setCellValue(u.reason());
      row.createCell(4).setCellValue(u.suggestedFix());
    }
  }

  private void writeClasspath(XSSFWorkbook wb, HierarchyReport report) {
    Sheet sheet = wb.createSheet("Classpath");
    header(sheet, "kind", "path", "readable");
    int r = 1;
    for (ClasspathEntry e : report.classpathAudit()) {
      Row row = sheet.createRow(r++);
      row.createCell(0).setCellValue(e.kind().name());
      row.createCell(1).setCellValue(e.path().toString());
      row.createCell(2).setCellValue(e.readable() ? "Y" : "N");
    }
  }

  private static int kv(Sheet sheet, int r, String k, String v) {
    if (r == 0) {
      header(sheet, "metric", "value");
      r = 1;
    }
    Row row = sheet.createRow(r);
    row.createCell(0).setCellValue(k);
    row.createCell(1).setCellValue(v);
    return r + 1;
  }

  private static void header(Sheet sheet, String... cols) {
    Row row = sheet.createRow(0);
    for (int i = 0; i < cols.length; i++) {
      row.createCell(i).setCellValue(cols[i]);
    }
  }

  private static final class ListBuffer {
    private final java.util.List<String[]> rows = new java.util.ArrayList<>();
  }
}

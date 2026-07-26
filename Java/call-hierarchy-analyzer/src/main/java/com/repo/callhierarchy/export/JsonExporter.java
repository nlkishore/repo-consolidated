package com.repo.callhierarchy.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.repo.callhierarchy.model.CallEdge;
import com.repo.callhierarchy.model.CallNode;
import com.repo.callhierarchy.model.ClassRef;
import com.repo.callhierarchy.model.ClasspathEntry;
import com.repo.callhierarchy.model.HierarchyReport;
import com.repo.callhierarchy.model.MethodRef;
import com.repo.callhierarchy.model.UnresolvedCall;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonExporter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(HierarchyReport report, Path outFile) throws IOException {
    Files.createDirectories(outFile.toAbsolutePath().getParent() == null
        ? Path.of(".")
        : outFile.toAbsolutePath().getParent());
    Files.writeString(outFile, gson.toJson(toMap(report)));
  }

  private Map<String, Object> toMap(HierarchyReport report) {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("entry", methodMap(report.entry()));
    root.put("classes", report.classes().stream().map(this::classMap).toList());
    root.put("edges", report.edges().stream().map(this::edgeMap).toList());
    root.put("root", nodeMap(report.root()));
    root.put("unresolved", report.unresolved().stream().map(this::unresolvedMap).toList());
    root.put("cycles", report.cycles());
    root.put(
        "classpathAudit",
        Map.of(
            "sourceRoots",
            report.classpathAudit().stream()
                .filter(e -> e.kind() == ClasspathEntry.Kind.SOURCE_ROOT)
                .map(e -> e.path().toString())
                .toList(),
            "jarsLoaded",
            report.classpathAudit().stream()
                .filter(e -> e.kind() == ClasspathEntry.Kind.JAR && e.readable())
                .map(e -> e.path().toString())
                .toList(),
            "jarsMissing",
            report.classpathAudit().stream()
                .filter(e -> e.kind() == ClasspathEntry.Kind.JAR && !e.readable())
                .map(e -> e.path().toString())
                .toList()));
    root.put(
        "meta",
        Map.of(
            "depthLimit",
            report.depthLimit(),
            "generatedAt",
            report.generatedAt().toString(),
            "jarClassCount",
            report.jarClassCount()));
    return root;
  }

  private Map<String, Object> methodMap(MethodRef m) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("typeFqn", m.typeFqn());
    map.put("methodName", m.methodName());
    map.put("paramTypeFqns", m.paramTypeFqns());
    map.put("origin", m.origin().name());
    map.put("jarName", m.jarName());
    map.put("sourceFile", m.sourceFile());
    map.put("line", m.line());
    return map;
  }

  private Map<String, Object> classMap(ClassRef c) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("typeFqn", c.typeFqn());
    map.put("simpleName", c.simpleName());
    map.put("package", c.packageName());
    map.put("origin", c.origin().name());
    map.put("jarName", c.jarName());
    map.put("firstDepth", c.firstDepth());
    map.put("roleHint", c.roleHint());
    return map;
  }

  private Map<String, Object> edgeMap(CallEdge e) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("from", e.from().key());
    map.put("to", e.to().key());
    map.put("kind", e.kind().name());
    map.put("callSiteLine", e.callSiteLine());
    map.put("depth", e.depth());
    map.put("calleeOrigin", e.to().origin().name());
    return map;
  }

  private Map<String, Object> nodeMap(CallNode n) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("method", methodMap(n.method()));
    map.put("depth", n.depth());
    map.put("leafReason", n.leafReason().name());
    map.put("path", n.path());
    List<Map<String, Object>> kids = new ArrayList<>();
    for (CallNode c : n.children()) {
      kids.add(nodeMap(c));
    }
    map.put("children", kids);
    return map;
  }

  private Map<String, Object> unresolvedMap(UnresolvedCall u) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("callerKey", u.callerKey());
    map.put("callText", u.callText());
    map.put("line", u.line());
    map.put("reason", u.reason());
    map.put("suggestedFix", u.suggestedFix());
    return map;
  }
}

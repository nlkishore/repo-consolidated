package com.repo.callhierarchy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.repo.callhierarchy.config.AnalyzerConfig;
import com.repo.callhierarchy.export.CsvExporter;
import com.repo.callhierarchy.export.ExcelExporter;
import com.repo.callhierarchy.model.HierarchyReport;
import com.repo.callhierarchy.model.Origin;
import com.repo.callhierarchy.resolve.EntryResolver;
import com.repo.callhierarchy.resolve.MethodRefParser;
import com.repo.callhierarchy.service.CallHierarchyService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CallHierarchyServiceTest {

  private static Path fixturesRoot;
  private static Path sampleAppRoot;
  private static Path sharedJar;
  private static Path workDir;

  @BeforeAll
  static void setup() throws Exception {
    fixturesRoot = Path.of(CallHierarchyServiceTest.class.getResource("/fixtures").toURI());
    sampleAppRoot = fixturesRoot.resolve("sample-app");
    workDir = Path.of("target/test-work").toAbsolutePath();
    deleteRecursive(workDir);
    Files.createDirectories(workDir);
    Path libSrc = fixturesRoot.resolve("sample-lib");
    Path compileOut = workDir.resolve("lib-classes");
    Files.createDirectories(compileOut);
    Path sharedJava = libSrc.resolve("com/bank/shared/SharedAudit.java");
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    int code =
        compiler.run(
            null,
            null,
            null,
            "-d",
            compileOut.toString(),
            sharedJava.toString());
    assertEquals(0, code, "SharedAudit must compile");
    sharedJar = workDir.resolve("bank-common-1.0.0.jar");
    jarDirectory(compileOut, sharedJar);
  }

  @Test
  void parsesEntryForms() {
    var p = MethodRefParser.parse("com.bank.web.OrderController#createOrder");
    assertEquals("com.bank.web.OrderController", p.typeFqn());
    assertEquals("createOrder", p.methodName());
    assertFalse(p.paramsSpecified());

    var p2 =
        MethodRefParser.parse(
            "com.bank.web.OrderController#createOrder(java.lang.String)");
    assertTrue(p2.paramsSpecified());
    assertEquals(1, p2.paramTypeFqns().size());
  }

  @Test
  void buildsHierarchyWithJarLeaf() throws Exception {
    Path appOnly = workDir.resolve("app-with-jar");
    deleteRecursive(appOnly);
    copyTree(sampleAppRoot, appOnly);

    AnalyzerConfig config =
        AnalyzerConfig.builder()
            .sourceRoot(appOnly)
            .jar(sharedJar)
            .excludePackage("java.")
            .excludePackage("javax.")
            .excludePackage("jakarta.")
            .maxDepth(10)
            .build();

    HierarchyReport report =
        new CallHierarchyService(config).analyze("com.bank.web.OrderController#createOrder");

    assertEquals("com.bank.web.OrderController", report.entry().typeFqn());
    assertTrue(
        report.classes().stream().anyMatch(c -> c.typeFqn().equals("com.bank.service.OrderService")));
    assertTrue(
        report.classes().stream()
            .anyMatch(
                c ->
                    c.typeFqn().equals("com.bank.shared.SharedAudit")
                        && c.origin() == Origin.JAR));
    assertTrue(report.edges().stream().anyMatch(e -> e.to().typeFqn().contains("SharedAudit")));
    assertTrue(report.jarClassCount() >= 1);

    Path outBase = workDir.resolve("out/hierarchy");
    Files.createDirectories(outBase.getParent());
    new CsvExporter().write(report, outBase);
    new ExcelExporter().write(report, Path.of(outBase + ".xlsx"));
    assertTrue(Files.exists(Path.of(outBase + ".xlsx")));
    assertTrue(Files.exists(Path.of(outBase + "-classes.csv")));
    String classesCsv = Files.readString(Path.of(outBase + "-classes.csv"));
    assertTrue(classesCsv.contains("SharedAudit"));
    assertTrue(classesCsv.contains("JAR"));
  }

  @Test
  void missingJarLeavesUnresolvedOrMissingShared() throws Exception {
    Path appOnly = workDir.resolve("app-no-jar");
    deleteRecursive(appOnly);
    copyTree(sampleAppRoot, appOnly);

    AnalyzerConfig config =
        AnalyzerConfig.builder()
            .sourceRoot(appOnly)
            .excludePackage("java.")
            .maxDepth(10)
            .build();

    HierarchyReport report =
        new CallHierarchyService(config).analyze("com.bank.web.OrderController#createOrder");

    boolean hasUnresolved = !report.unresolved().isEmpty();
    boolean missingShared =
        report.classes().stream().noneMatch(c -> c.typeFqn().equals("com.bank.shared.SharedAudit"));
    assertTrue(
        hasUnresolved || missingShared,
        "Without JAR, SharedAudit should be unresolved or absent from resolved classes");
  }

  @Test
  void ambiguousOrMissingEntryFails() {
    AnalyzerConfig config =
        AnalyzerConfig.builder().sourceRoot(sampleAppRoot).jar(sharedJar).build();
    assertThrows(
        EntryResolver.EntryResolutionException.class,
        () -> new CallHierarchyService(config).analyze("com.bank.web.Missing#nope"));
  }

  private static void deleteRecursive(Path root) throws IOException {
    if (!Files.exists(root)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(root)) {
      walk.sorted(Comparator.reverseOrder())
          .forEach(
              p -> {
                try {
                  Files.deleteIfExists(p);
                } catch (IOException ignored) {
                  // Windows may keep JARs open via JarTypeSolver; ignore cleanup.
                }
              });
    }
  }

  private static void copyTree(Path src, Path dest) throws IOException {
    try (Stream<Path> walk = Files.walk(src)) {
      walk.forEach(
          p -> {
            try {
              Path rel = src.relativize(p);
              Path target = dest.resolve(rel.toString());
              if (Files.isDirectory(p)) {
                Files.createDirectories(target);
              } else {
                Files.createDirectories(target.getParent());
                Files.copy(p, target);
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    }
  }

  private static void jarDirectory(Path classesDir, Path jarFile) throws IOException {
    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile));
        Stream<Path> walk = Files.walk(classesDir)) {
      walk.filter(Files::isRegularFile)
          .sorted(Comparator.comparing(Path::toString))
          .forEach(
              p -> {
                try {
                  String entryName =
                      classesDir.relativize(p).toString().replace('\\', '/');
                  jos.putNextEntry(new JarEntry(entryName));
                  Files.copy(p, jos);
                  jos.closeEntry();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }
}

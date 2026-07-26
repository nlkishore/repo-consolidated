package com.repo.callhierarchy.cli;

import com.repo.callhierarchy.config.AnalyzerConfig;
import com.repo.callhierarchy.export.CsvExporter;
import com.repo.callhierarchy.export.ExcelExporter;
import com.repo.callhierarchy.export.JsonExporter;
import com.repo.callhierarchy.export.MermaidExporter;
import com.repo.callhierarchy.model.HierarchyReport;
import com.repo.callhierarchy.resolve.EntryResolver;
import com.repo.callhierarchy.service.CallHierarchyService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "call-hierarchy-analyzer",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Build class/method call hierarchy from an entry class method (JavaParser).")
public final class CallHierarchyCli implements Callable<Integer> {
  private static final Logger LOG = Logger.getLogger(CallHierarchyCli.class.getName());

  @Option(
      names = {"--entry", "-e"},
      required = true,
      description = "Entry method, e.g. com.bank.web.OrderController#createOrder")
  private String entry;

  @Option(
      names = {"--source", "-s"},
      required = true,
      split = ",",
      description = "Source root(s). Repeatable or comma-separated.")
  private List<Path> sources = new ArrayList<>();

  @Option(
      names = {"--jar", "-j"},
      description = "Supporting JAR file(s). Repeatable.")
  private List<Path> jars = new ArrayList<>();

  @Option(
      names = {"--lib-dir"},
      description = "Directory of JARs to scan. Repeatable.")
  private List<Path> libDirs = new ArrayList<>();

  @Option(
      names = {"--classpath-file"},
      description = "Classpath file (one path per line, or ; / : separated).")
  private Path classpathFile;

  @Option(
      names = {"--include-package"},
      description = "Only include types with this package prefix. Repeatable.")
  private List<String> includePackages = new ArrayList<>();

  @Option(
      names = {"--exclude-package"},
      description = "Exclude package prefix. Repeatable. Defaults include java./javax./jakarta.")
  private List<String> excludePackages = new ArrayList<>();

  @Option(
      names = {"--max-depth"},
      description = "Max recursion depth (default: ${DEFAULT-VALUE}).",
      defaultValue = "20")
  private int maxDepth;

  @Option(
      names = {"--format", "-f"},
      description = "Comma-separated: excel,csv,json,mermaid (default: excel,csv).",
      defaultValue = "excel,csv")
  private String format;

  @Option(
      names = {"--out", "-o"},
      required = true,
      description = "Output base path without extension (e.g. out/hierarchy).")
  private Path out;

  @Option(
      names = {"--strict-classpath"},
      description = "Fail if any configured JAR path is missing.",
      defaultValue = "false")
  private boolean strictClasspath;

  public static void main(String[] args) {
    int code = new CommandLine(new CallHierarchyCli()).execute(args);
    System.exit(code);
  }

  @Override
  public Integer call() {
    try {
      AnalyzerConfig.Builder builder =
          AnalyzerConfig.builder()
              .sourceRoots(sources)
              .jars(jars)
              .libDirs(libDirs)
              .classpathFile(classpathFile)
              .maxDepth(maxDepth)
              .strictClasspath(strictClasspath);
      includePackages.forEach(builder::includePackage);
      excludePackages.forEach(builder::excludePackage);
      AnalyzerConfig config = builder.build();

      HierarchyReport report = new CallHierarchyService(config).analyze(entry);
      writeOutputs(report);
      System.out.println(
          "OK entry="
              + report.entry().key()
              + " classes="
              + report.classes().size()
              + " edges="
              + report.edges().size()
              + " unresolved="
              + report.unresolved().size()
              + " out="
              + out);
      return 0;
    } catch (EntryResolver.EntryResolutionException e) {
      System.err.println(e.getMessage());
      return e.exitCode();
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      return 2;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Analysis failed", e);
      System.err.println("ERROR: " + e.getMessage());
      return 3;
    }
  }

  private void writeOutputs(HierarchyReport report) throws Exception {
    List<String> formats =
        Arrays.stream(format.split(","))
            .map(s -> s.trim().toLowerCase(Locale.ROOT))
            .filter(s -> !s.isEmpty())
            .toList();
    if (formats.isEmpty()) {
      formats = List.of("excel", "csv");
    }
    for (String f : formats) {
      switch (f) {
        case "excel", "xlsx" -> new ExcelExporter().write(report, Path.of(out + ".xlsx"));
        case "csv" -> new CsvExporter().write(report, out);
        case "json" -> new JsonExporter().write(report, Path.of(out + ".json"));
        case "mermaid", "md" -> new MermaidExporter().write(report, Path.of(out + ".md"));
        default -> throw new IllegalArgumentException("Unknown format: " + f);
      }
    }
  }
}

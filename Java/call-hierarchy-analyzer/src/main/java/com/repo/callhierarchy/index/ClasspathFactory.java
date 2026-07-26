package com.repo.callhierarchy.index;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.repo.callhierarchy.config.AnalyzerConfig;
import com.repo.callhierarchy.model.ClasspathEntry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Builds CombinedTypeSolver and maps class FQNs found in JARs to jar file names.
 */
public final class ClasspathFactory {

  public record Result(
      CombinedTypeSolver typeSolver,
      ParserConfiguration parserConfiguration,
      Map<String, String> classFqnToJarName,
      List<ClasspathEntry> audit,
      List<Path> loadedJars) {}

  private ClasspathFactory() {}

  public static Result create(AnalyzerConfig config) throws IOException {
    CombinedTypeSolver combined = new CombinedTypeSolver();
    List<ClasspathEntry> audit = new ArrayList<>();
    Map<String, String> classToJar = new LinkedHashMap<>();
    Set<Path> jarPaths = new LinkedHashSet<>();

    for (Path root : config.sourceRoots()) {
      boolean ok = Files.isDirectory(root);
      audit.add(new ClasspathEntry(ClasspathEntry.Kind.SOURCE_ROOT, root.toAbsolutePath(), ok));
      if (ok) {
        combined.add(new JavaParserTypeSolver(root));
      }
    }

    jarPaths.addAll(config.jars());
    for (Path libDir : config.libDirs()) {
      jarPaths.addAll(scanLibDir(libDir, config.libDirRecursive()));
    }
    if (config.classpathFile() != null) {
      jarPaths.addAll(readClasspathFile(config.classpathFile()));
    }

    List<Path> loaded = new ArrayList<>();
    for (Path jar : jarPaths) {
      Path abs = jar.toAbsolutePath().normalize();
      boolean ok = Files.isRegularFile(abs) && abs.toString().toLowerCase(Locale.ROOT).endsWith(".jar");
      audit.add(new ClasspathEntry(ClasspathEntry.Kind.JAR, abs, ok));
      if (!ok) {
        continue;
      }
      combined.add(new JarTypeSolver(abs));
      indexJarClasses(abs, classToJar);
      loaded.add(abs);
    }

    combined.add(new ReflectionTypeSolver(true));

    ParserConfiguration parserConfiguration = new ParserConfiguration();
    parserConfiguration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    parserConfiguration.setSymbolResolver(new JavaSymbolSolver(combined));

    return new Result(
        combined,
        parserConfiguration,
        Collections.unmodifiableMap(classToJar),
        Collections.unmodifiableList(audit),
        Collections.unmodifiableList(loaded));
  }

  private static List<Path> scanLibDir(Path libDir, boolean recursive) throws IOException {
    if (!Files.isDirectory(libDir)) {
      return List.of();
    }
    try (Stream<Path> stream = recursive ? Files.walk(libDir) : Files.list(libDir)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
          .sorted()
          .collect(Collectors.toList());
    }
  }

  private static List<Path> readClasspathFile(Path file) throws IOException {
    if (!Files.isRegularFile(file)) {
      return List.of();
    }
    String content = Files.readString(file, StandardCharsets.UTF_8).trim();
    if (content.isEmpty()) {
      return List.of();
    }
    List<Path> paths = new ArrayList<>();
    // Prefer one path per line; also accept ; or : separated single line.
    if (content.contains("\n")) {
      for (String line : content.split("\\R")) {
        String t = line.trim();
        if (!t.isEmpty() && !t.startsWith("#")) {
          paths.add(Path.of(t));
        }
      }
    } else if (content.contains(";") || content.contains(":")) {
      // Windows-style ; takes precedence if present.
      String sep = content.contains(";") ? ";" : ":";
      for (String part : content.split(sep)) {
        String t = part.trim();
        if (!t.isEmpty()) {
          paths.add(Path.of(t));
        }
      }
    } else {
      paths.add(Path.of(content));
    }
    return paths;
  }

  private static void indexJarClasses(Path jar, Map<String, String> classToJar) throws IOException {
    String jarName = jar.getFileName().toString();
    try (ZipFile zip = new ZipFile(jar.toFile())) {
      var entries = zip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        if (!name.endsWith(".class") || name.contains("$")) {
          continue;
        }
        String fqn = name.substring(0, name.length() - 6).replace('/', '.');
        classToJar.putIfAbsent(fqn, jarName);
      }
    }
  }
}

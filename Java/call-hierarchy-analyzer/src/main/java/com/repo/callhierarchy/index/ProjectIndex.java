package com.repo.callhierarchy.index;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.repo.callhierarchy.config.AnalyzerConfig;
import com.repo.callhierarchy.model.ClasspathEntry;
import com.repo.callhierarchy.model.MethodRef;
import com.repo.callhierarchy.model.Origin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProjectIndex {
  private final AnalyzerConfig config;
  private final ClasspathFactory.Result classpath;
  private final JavaParser javaParser;
  private final List<CompilationUnit> compilationUnits;
  private final Map<String, MethodDeclaration> methodsByKey;
  private final Map<String, Path> typeFqnToSourceFile;

  private ProjectIndex(
      AnalyzerConfig config,
      ClasspathFactory.Result classpath,
      JavaParser javaParser,
      List<CompilationUnit> compilationUnits,
      Map<String, MethodDeclaration> methodsByKey,
      Map<String, Path> typeFqnToSourceFile) {
    this.config = config;
    this.classpath = classpath;
    this.javaParser = javaParser;
    this.compilationUnits = compilationUnits;
    this.methodsByKey = methodsByKey;
    this.typeFqnToSourceFile = typeFqnToSourceFile;
  }

  public static ProjectIndex build(AnalyzerConfig config) throws IOException {
    ClasspathFactory.Result cp = ClasspathFactory.create(config);
    JavaParser parser = new JavaParser(cp.parserConfiguration());
    List<CompilationUnit> cus = new ArrayList<>();
    Map<String, MethodDeclaration> methods = new HashMap<>();
    Map<String, Path> typeFiles = new HashMap<>();

    for (Path root : config.sourceRoots()) {
      if (!Files.isDirectory(root)) {
        continue;
      }
      try (Stream<Path> walk = Files.walk(root)) {
        List<Path> javaFiles =
            walk.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .sorted()
                .collect(Collectors.toList());
        for (Path file : javaFiles) {
          ParseResult<CompilationUnit> result = parser.parse(file);
          if (!result.isSuccessful() || result.getResult().isEmpty()) {
            continue;
          }
          CompilationUnit cu = result.getResult().get();
          cus.add(cu);
          indexCu(cu, file, root, methods, typeFiles);
        }
      }
    }

    return new ProjectIndex(
        config,
        cp,
        parser,
        Collections.unmodifiableList(cus),
        Collections.unmodifiableMap(methods),
        Collections.unmodifiableMap(typeFiles));
  }

  private static void indexCu(
      CompilationUnit cu,
      Path file,
      Path root,
      Map<String, MethodDeclaration> methods,
      Map<String, Path> typeFiles) {
    for (TypeDeclaration<?> type : cu.getTypes()) {
      indexType(type, file, root, methods, typeFiles, cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse(""));
    }
  }

  private static void indexType(
      TypeDeclaration<?> type,
      Path file,
      Path root,
      Map<String, MethodDeclaration> methods,
      Map<String, Path> typeFiles,
      String pkg) {
    String simple = type.getNameAsString();
    String fqn = pkg.isEmpty() ? simple : pkg + "." + simple;
    typeFiles.putIfAbsent(fqn, relativize(root, file));

    for (MethodDeclaration md : type.getMethods()) {
      String key = methodKey(fqn, md);
      methods.putIfAbsent(key, md);
      // Also store name-only key when unique enough is handled by EntryResolver.
      methods.putIfAbsent(fqn + "#" + md.getNameAsString(), md);
    }

    if (type instanceof ClassOrInterfaceDeclaration coid) {
      for (TypeDeclaration<?> nested : coid.getMembers().stream()
          .filter(TypeDeclaration.class::isInstance)
          .map(TypeDeclaration.class::cast)
          .toList()) {
        indexType(nested, file, root, methods, typeFiles, fqn);
      }
    }
  }

  public static String methodKey(String typeFqn, MethodDeclaration md) {
    List<String> params = new ArrayList<>();
    md.getParameters()
        .forEach(
            p -> {
              try {
                ResolvedType rt = p.getType().resolve();
                params.add(describeResolvedType(rt));
              } catch (Exception e) {
                params.add(p.getType().asString());
              }
            });
    return typeFqn + "#" + md.getNameAsString() + "(" + String.join(",", params) + ")";
  }

  public static String describeResolvedType(ResolvedType rt) {
    if (rt.isArray()) {
      return describeResolvedType(rt.asArrayType().getComponentType()) + "[]";
    }
    if (rt.isPrimitive()) {
      return rt.asPrimitive().describe();
    }
    if (rt.isReferenceType()) {
      return rt.asReferenceType().getQualifiedName();
    }
    return rt.describe();
  }

  private static Path relativize(Path root, Path file) {
    try {
      return root.toAbsolutePath().normalize().relativize(file.toAbsolutePath().normalize());
    } catch (Exception e) {
      return file.getFileName();
    }
  }

  public AnalyzerConfig config() {
    return config;
  }

  public ClasspathFactory.Result classpath() {
    return classpath;
  }

  public ParserConfiguration parserConfiguration() {
    return classpath.parserConfiguration();
  }

  public Optional<MethodDeclaration> findMethod(String key) {
    return Optional.ofNullable(methodsByKey.get(key));
  }

  public List<MethodDeclaration> findMethodsByName(String typeFqn, String methodName) {
    String prefix = typeFqn + "#" + methodName;
    List<MethodDeclaration> found = new ArrayList<>();
    for (Map.Entry<String, MethodDeclaration> e : methodsByKey.entrySet()) {
      String k = e.getKey();
      if (k.equals(prefix) || (k.startsWith(prefix + "(") && k.contains("("))) {
        if (!found.contains(e.getValue())) {
          found.add(e.getValue());
        }
      }
    }
    return found;
  }

  public boolean isSourceType(String typeFqn) {
    return typeFqnToSourceFile.containsKey(typeFqn);
  }

  public Origin originOf(String typeFqn) {
    if (typeFqn == null) {
      return Origin.UNKNOWN;
    }
    if (isSourceType(typeFqn)) {
      return Origin.SOURCE;
    }
    if (classpath.classFqnToJarName().containsKey(typeFqn)) {
      return Origin.JAR;
    }
    if (typeFqn.startsWith("java.")
        || typeFqn.startsWith("javax.")
        || typeFqn.startsWith("jakarta.")
        || typeFqn.startsWith("sun.")
        || typeFqn.startsWith("jdk.")) {
      return Origin.JDK;
    }
    return Origin.UNKNOWN;
  }

  public String jarNameOf(String typeFqn) {
    return classpath.classFqnToJarName().get(typeFqn);
  }

  public List<ClasspathEntry> classpathAudit() {
    return classpath.audit();
  }

  public MethodRef toMethodRef(String typeFqn, MethodDeclaration md) {
    List<String> params = new ArrayList<>();
    md.getParameters()
        .forEach(
            p -> {
              try {
                params.add(describeResolvedType(p.getType().resolve()));
              } catch (Exception e) {
                params.add(p.getType().asString());
              }
            });
    String ret = null;
    try {
      ret = describeResolvedType(md.getType().resolve());
    } catch (Exception ignored) {
      ret = md.getType().asString();
    }
    Integer line = md.getBegin().map(p -> p.line).orElse(null);
    Path src = typeFqnToSourceFile.get(typeFqn);
    return new MethodRef(
        typeFqn,
        md.getNameAsString(),
        params,
        ret,
        src == null ? null : src.toString().replace('\\', '/'),
        line,
        Origin.SOURCE,
        null);
  }
}

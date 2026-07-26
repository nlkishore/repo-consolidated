package com.repo.callhierarchy.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AnalyzerConfig {
  private final List<Path> sourceRoots;
  private final List<Path> jars;
  private final List<Path> libDirs;
  private final Path classpathFile;
  private final List<String> includePackages;
  private final List<String> excludePackages;
  private final int maxDepth;
  private final boolean strictClasspath;
  private final boolean libDirRecursive;

  private AnalyzerConfig(Builder b) {
    this.sourceRoots = List.copyOf(b.sourceRoots);
    this.jars = List.copyOf(b.jars);
    this.libDirs = List.copyOf(b.libDirs);
    this.classpathFile = b.classpathFile;
    this.includePackages = List.copyOf(b.includePackages);
    this.excludePackages = List.copyOf(b.excludePackages);
    this.maxDepth = b.maxDepth;
    this.strictClasspath = b.strictClasspath;
    this.libDirRecursive = b.libDirRecursive;
  }

  public static Builder builder() {
    return new Builder();
  }

  public List<Path> sourceRoots() {
    return sourceRoots;
  }

  public List<Path> jars() {
    return jars;
  }

  public List<Path> libDirs() {
    return libDirs;
  }

  public Path classpathFile() {
    return classpathFile;
  }

  public List<String> includePackages() {
    return includePackages;
  }

  public List<String> excludePackages() {
    return excludePackages;
  }

  public int maxDepth() {
    return maxDepth;
  }

  public boolean strictClasspath() {
    return strictClasspath;
  }

  public boolean libDirRecursive() {
    return libDirRecursive;
  }

  public boolean isExcluded(String typeFqn) {
    if (typeFqn == null) {
      return true;
    }
    for (String ex : excludePackages) {
      if (typeFqn.startsWith(ex)) {
        return true;
      }
    }
    if (includePackages.isEmpty()) {
      return false;
    }
    for (String in : includePackages) {
      if (typeFqn.startsWith(in)) {
        return false;
      }
    }
    return true;
  }

  public static final class Builder {
    private final List<Path> sourceRoots = new ArrayList<>();
    private final List<Path> jars = new ArrayList<>();
    private final List<Path> libDirs = new ArrayList<>();
    private Path classpathFile;
    private final List<String> includePackages = new ArrayList<>();
    private final List<String> excludePackages = new ArrayList<>();
    private int maxDepth = 20;
    private boolean strictClasspath;
    private boolean libDirRecursive = true;

    public Builder sourceRoot(Path p) {
      if (p != null) {
        sourceRoots.add(p);
      }
      return this;
    }

    public Builder sourceRoots(List<Path> paths) {
      if (paths != null) {
        sourceRoots.addAll(paths);
      }
      return this;
    }

    public Builder jar(Path p) {
      if (p != null) {
        jars.add(p);
      }
      return this;
    }

    public Builder jars(List<Path> paths) {
      if (paths != null) {
        jars.addAll(paths);
      }
      return this;
    }

    public Builder libDir(Path p) {
      if (p != null) {
        libDirs.add(p);
      }
      return this;
    }

    public Builder libDirs(List<Path> paths) {
      if (paths != null) {
        libDirs.addAll(paths);
      }
      return this;
    }

    public Builder classpathFile(Path p) {
      this.classpathFile = p;
      return this;
    }

    public Builder includePackage(String p) {
      if (p != null && !p.isBlank()) {
        includePackages.add(p);
      }
      return this;
    }

    public Builder excludePackage(String p) {
      if (p != null && !p.isBlank()) {
        excludePackages.add(p);
      }
      return this;
    }

    public Builder maxDepth(int d) {
      this.maxDepth = Math.max(0, d);
      return this;
    }

    public Builder strictClasspath(boolean v) {
      this.strictClasspath = v;
      return this;
    }

    public Builder libDirRecursive(boolean v) {
      this.libDirRecursive = v;
      return this;
    }

    public AnalyzerConfig build() {
      if (sourceRoots.isEmpty()) {
        throw new IllegalArgumentException("At least one --source root is required");
      }
      if (excludePackages.isEmpty()) {
        excludePackages.add("java.");
        excludePackages.add("javax.");
        excludePackages.add("jakarta.");
        excludePackages.add("sun.");
        excludePackages.add("jdk.");
      }
      Set<Path> dedupSources = new LinkedHashSet<>(sourceRoots);
      sourceRoots.clear();
      sourceRoots.addAll(dedupSources);
      return new AnalyzerConfig(this);
    }
  }
}

package com.repo.callhierarchy.model;

import java.nio.file.Path;

public final class ClasspathEntry {
  public enum Kind {
    SOURCE_ROOT,
    JAR
  }

  private final Kind kind;
  private final Path path;
  private final boolean readable;

  public ClasspathEntry(Kind kind, Path path, boolean readable) {
    this.kind = kind;
    this.path = path;
    this.readable = readable;
  }

  public Kind kind() {
    return kind;
  }

  public Path path() {
    return path;
  }

  public boolean readable() {
    return readable;
  }
}

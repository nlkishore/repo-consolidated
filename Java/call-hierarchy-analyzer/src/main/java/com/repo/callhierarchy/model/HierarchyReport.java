package com.repo.callhierarchy.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HierarchyReport {
  private final MethodRef entry;
  private final List<ClassRef> classes;
  private final List<CallEdge> edges;
  private final CallNode root;
  private final List<UnresolvedCall> unresolved;
  private final List<String> cycles;
  private final List<ClasspathEntry> classpathAudit;
  private final int depthLimit;
  private final Instant generatedAt;

  public HierarchyReport(
      MethodRef entry,
      List<ClassRef> classes,
      List<CallEdge> edges,
      CallNode root,
      List<UnresolvedCall> unresolved,
      List<String> cycles,
      List<ClasspathEntry> classpathAudit,
      int depthLimit,
      Instant generatedAt) {
    this.entry = entry;
    this.classes = freeze(classes);
    this.edges = freeze(edges);
    this.root = root;
    this.unresolved = freeze(unresolved);
    this.cycles = freeze(cycles);
    this.classpathAudit = freeze(classpathAudit);
    this.depthLimit = depthLimit;
    this.generatedAt = generatedAt == null ? Instant.now() : generatedAt;
  }

  private static <T> List<T> freeze(List<T> in) {
    return in == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(in));
  }

  public MethodRef entry() {
    return entry;
  }

  public List<ClassRef> classes() {
    return classes;
  }

  public List<CallEdge> edges() {
    return edges;
  }

  public CallNode root() {
    return root;
  }

  public List<UnresolvedCall> unresolved() {
    return unresolved;
  }

  public List<String> cycles() {
    return cycles;
  }

  public List<ClasspathEntry> classpathAudit() {
    return classpathAudit;
  }

  public int depthLimit() {
    return depthLimit;
  }

  public Instant generatedAt() {
    return generatedAt;
  }

  public long jarClassCount() {
    return classes.stream().filter(c -> c.origin() == Origin.JAR).count();
  }
}

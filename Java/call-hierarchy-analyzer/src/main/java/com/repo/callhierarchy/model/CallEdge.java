package com.repo.callhierarchy.model;

public final class CallEdge {
  public enum Kind {
    INVOKE,
    NEW,
    SUPER
  }

  private final MethodRef from;
  private final MethodRef to;
  private final Kind kind;
  private final Integer callSiteLine;
  private final int depth;

  public CallEdge(MethodRef from, MethodRef to, Kind kind, Integer callSiteLine, int depth) {
    this.from = from;
    this.to = to;
    this.kind = kind == null ? Kind.INVOKE : kind;
    this.callSiteLine = callSiteLine;
    this.depth = depth;
  }

  public MethodRef from() {
    return from;
  }

  public MethodRef to() {
    return to;
  }

  public Kind kind() {
    return kind;
  }

  public Integer callSiteLine() {
    return callSiteLine;
  }

  public int depth() {
    return depth;
  }
}

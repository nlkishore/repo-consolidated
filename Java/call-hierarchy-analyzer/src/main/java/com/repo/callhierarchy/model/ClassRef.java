package com.repo.callhierarchy.model;

public final class ClassRef {
  private final String typeFqn;
  private final Origin origin;
  private final String jarName;
  private final int firstDepth;
  private final String roleHint;

  public ClassRef(String typeFqn, Origin origin, String jarName, int firstDepth, String roleHint) {
    this.typeFqn = typeFqn;
    this.origin = origin == null ? Origin.UNKNOWN : origin;
    this.jarName = jarName;
    this.firstDepth = firstDepth;
    this.roleHint = roleHint;
  }

  public String typeFqn() {
    return typeFqn;
  }

  public Origin origin() {
    return origin;
  }

  public String jarName() {
    return jarName;
  }

  public int firstDepth() {
    return firstDepth;
  }

  public String roleHint() {
    return roleHint;
  }

  public String packageName() {
    int idx = typeFqn.lastIndexOf('.');
    return idx < 0 ? "" : typeFqn.substring(0, idx);
  }

  public String simpleName() {
    int idx = typeFqn.lastIndexOf('.');
    return idx < 0 ? typeFqn : typeFqn.substring(idx + 1);
  }
}

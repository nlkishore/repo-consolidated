package com.repo.callhierarchy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CallNode {
  private final MethodRef method;
  private final int depth;
  private final LeafReason leafReason;
  private final List<CallNode> children;
  private final String path;

  public CallNode(
      MethodRef method, int depth, LeafReason leafReason, List<CallNode> children, String path) {
    this.method = method;
    this.depth = depth;
    this.leafReason = leafReason == null ? LeafReason.NONE : leafReason;
    this.children =
        children == null
            ? List.of()
            : Collections.unmodifiableList(new ArrayList<>(children));
    this.path = path;
  }

  public MethodRef method() {
    return method;
  }

  public int depth() {
    return depth;
  }

  public LeafReason leafReason() {
    return leafReason;
  }

  public List<CallNode> children() {
    return children;
  }

  public String path() {
    return path;
  }
}

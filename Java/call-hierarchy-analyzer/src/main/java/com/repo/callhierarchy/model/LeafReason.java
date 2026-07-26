package com.repo.callhierarchy.model;

public enum LeafReason {
  NONE,
  SOURCE_END,
  JAR_LEAF,
  FILTERED,
  UNRESOLVED,
  CYCLE,
  DEPTH_LIMIT
}

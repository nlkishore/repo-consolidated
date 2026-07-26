package com.repo.callhierarchy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MethodRef {
  private final String typeFqn;
  private final String methodName;
  private final List<String> paramTypeFqns;
  private final String returnTypeFqn;
  private final String sourceFile;
  private final Integer line;
  private final Origin origin;
  private final String jarName;

  public MethodRef(
      String typeFqn,
      String methodName,
      List<String> paramTypeFqns,
      String returnTypeFqn,
      String sourceFile,
      Integer line,
      Origin origin,
      String jarName) {
    this.typeFqn = typeFqn;
    this.methodName = methodName;
    this.paramTypeFqns =
        paramTypeFqns == null
            ? List.of()
            : Collections.unmodifiableList(new ArrayList<>(paramTypeFqns));
    this.returnTypeFqn = returnTypeFqn;
    this.sourceFile = sourceFile;
    this.line = line;
    this.origin = origin == null ? Origin.UNKNOWN : origin;
    this.jarName = jarName;
  }

  public static MethodRef of(String typeFqn, String methodName, List<String> params, Origin origin) {
    return new MethodRef(typeFqn, methodName, params, null, null, null, origin, null);
  }

  public String typeFqn() {
    return typeFqn;
  }

  public String methodName() {
    return methodName;
  }

  public List<String> paramTypeFqns() {
    return paramTypeFqns;
  }

  public String returnTypeFqn() {
    return returnTypeFqn;
  }

  public String sourceFile() {
    return sourceFile;
  }

  public Integer line() {
    return line;
  }

  public Origin origin() {
    return origin;
  }

  public String jarName() {
    return jarName;
  }

  public String key() {
    return typeFqn
        + "#"
        + methodName
        + "("
        + paramTypeFqns.stream().collect(Collectors.joining(","))
        + ")";
  }

  public String shortLabel() {
    String simple =
        typeFqn == null || !typeFqn.contains(".")
            ? typeFqn
            : typeFqn.substring(typeFqn.lastIndexOf('.') + 1);
    return simple + "#" + methodName;
  }

  public MethodRef withOrigin(Origin newOrigin, String newJarName) {
    return new MethodRef(
        typeFqn, methodName, paramTypeFqns, returnTypeFqn, sourceFile, line, newOrigin, newJarName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MethodRef that)) {
      return false;
    }
    return Objects.equals(key(), that.key());
  }

  @Override
  public int hashCode() {
    return Objects.hash(key());
  }

  @Override
  public String toString() {
    return key();
  }
}

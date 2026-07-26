package com.repo.callhierarchy.model;

public final class UnresolvedCall {
  private final String callerKey;
  private final String callText;
  private final Integer line;
  private final String reason;
  private final String suggestedFix;

  public UnresolvedCall(
      String callerKey, String callText, Integer line, String reason, String suggestedFix) {
    this.callerKey = callerKey;
    this.callText = callText;
    this.line = line;
    this.reason = reason;
    this.suggestedFix = suggestedFix;
  }

  public String callerKey() {
    return callerKey;
  }

  public String callText() {
    return callText;
  }

  public Integer line() {
    return line;
  }

  public String reason() {
    return reason;
  }

  public String suggestedFix() {
    return suggestedFix;
  }
}

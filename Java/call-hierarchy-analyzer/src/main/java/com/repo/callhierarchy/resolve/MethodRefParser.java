package com.repo.callhierarchy.resolve;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses entry strings like {@code com.bank.web.OrderController#createOrder(com.bank.api.OrderRequest)}. */
public final class MethodRefParser {
  private static final Pattern PATTERN =
      Pattern.compile(
          "^(?<type>[\\w.$]+)\\s*(?:#|::)\\s*(?<method>[\\w$]+)(?:\\((?<params>.*)\\))?$");

  public record ParsedEntry(String typeFqn, String methodName, List<String> paramTypeFqns, boolean paramsSpecified) {}

  private MethodRefParser() {}

  public static ParsedEntry parse(String entry) {
    if (entry == null || entry.isBlank()) {
      throw new IllegalArgumentException("Entry must not be blank");
    }
    String trimmed = entry.trim();
    Matcher m = PATTERN.matcher(trimmed);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Invalid entry format. Expected TypeFqn#method or TypeFqn#method(paramTypes). Got: "
              + entry);
    }
    String type = m.group("type");
    String method = m.group("method");
    String paramsGroup = m.group("params");
    boolean specified = trimmed.contains("(");
    List<String> params = new ArrayList<>();
    if (paramsGroup != null && !paramsGroup.isBlank()) {
      for (String part : splitParams(paramsGroup)) {
        String p = part.trim();
        if (!p.isEmpty()) {
          params.add(p);
        }
      }
    }
    return new ParsedEntry(type, method, params, specified);
  }

  private static List<String> splitParams(String paramsGroup) {
    List<String> parts = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    int depth = 0;
    for (int i = 0; i < paramsGroup.length(); i++) {
      char c = paramsGroup.charAt(i);
      if (c == '<') {
        depth++;
        cur.append(c);
      } else if (c == '>') {
        depth--;
        cur.append(c);
      } else if (c == ',' && depth == 0) {
        parts.add(cur.toString());
        cur.setLength(0);
      } else {
        cur.append(c);
      }
    }
    if (!cur.isEmpty()) {
      parts.add(cur.toString());
    }
    return parts;
  }
}

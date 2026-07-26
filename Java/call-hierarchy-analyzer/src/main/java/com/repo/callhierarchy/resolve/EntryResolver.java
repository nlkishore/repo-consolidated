package com.repo.callhierarchy.resolve;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.repo.callhierarchy.index.ProjectIndex;
import com.repo.callhierarchy.model.MethodRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class EntryResolver {
  public record ResolvedEntry(MethodRef methodRef, MethodDeclaration declaration) {}

  public static final class EntryResolutionException extends RuntimeException {
    private final int exitCode;

    public EntryResolutionException(String message, int exitCode) {
      super(message);
      this.exitCode = exitCode;
    }

    public int exitCode() {
      return exitCode;
    }
  }

  private final ProjectIndex index;

  public EntryResolver(ProjectIndex index) {
    this.index = index;
  }

  public ResolvedEntry resolve(String entrySpec) {
    MethodRefParser.ParsedEntry parsed = MethodRefParser.parse(entrySpec);
    if (!index.isSourceType(parsed.typeFqn())) {
      throw new EntryResolutionException(
          "Entry class not found in source roots: "
              + parsed.typeFqn()
              + ". Searched: "
              + index.config().sourceRoots(),
          2);
    }

    List<MethodDeclaration> candidates =
        index.findMethodsByName(parsed.typeFqn(), parsed.methodName());
    // Prefer fully keyed entries
    List<MethodDeclaration> unique = new ArrayList<>();
    for (MethodDeclaration md : candidates) {
      if (!unique.contains(md)) {
        unique.add(md);
      }
    }

    if (unique.isEmpty()) {
      throw new EntryResolutionException(
          "Method not found: " + parsed.typeFqn() + "#" + parsed.methodName(), 2);
    }

    if (parsed.paramsSpecified()) {
      MethodDeclaration match = null;
      for (MethodDeclaration md : unique) {
        MethodRef ref = index.toMethodRef(parsed.typeFqn(), md);
        if (paramsMatch(ref.paramTypeFqns(), parsed.paramTypeFqns())) {
          match = md;
          break;
        }
      }
      if (match == null) {
        throw new EntryResolutionException(
            "No overload matches parameters "
                + parsed.paramTypeFqns()
                + ". Candidates: "
                + describe(unique, parsed.typeFqn()),
            2);
      }
      return new ResolvedEntry(index.toMethodRef(parsed.typeFqn(), match), match);
    }

    if (unique.size() > 1) {
      throw new EntryResolutionException(
          "Ambiguous method overloads for "
              + parsed.typeFqn()
              + "#"
              + parsed.methodName()
              + ". Specify parameters. Candidates: "
              + describe(unique, parsed.typeFqn()),
          2);
    }

    MethodDeclaration md = unique.get(0);
    return new ResolvedEntry(index.toMethodRef(parsed.typeFqn(), md), md);
  }

  private static boolean paramsMatch(List<String> actual, List<String> expected) {
    if (actual.size() != expected.size()) {
      return false;
    }
    for (int i = 0; i < actual.size(); i++) {
      if (!normalize(actual.get(i)).equals(normalize(expected.get(i)))) {
        // Allow simple name match against FQN
        String a = normalize(actual.get(i));
        String e = normalize(expected.get(i));
        if (!(a.endsWith("." + e) || e.endsWith("." + a) || simple(a).equals(simple(e)))) {
          return false;
        }
      }
    }
    return true;
  }

  private static String normalize(String s) {
    return s == null ? "" : s.replace(" ", "").toLowerCase(Locale.ROOT);
  }

  private static String simple(String fqn) {
    int idx = fqn.lastIndexOf('.');
    return idx < 0 ? fqn : fqn.substring(idx + 1);
  }

  private String describe(List<MethodDeclaration> mds, String typeFqn) {
    return mds.stream()
        .map(md -> index.toMethodRef(typeFqn, md).key())
        .collect(Collectors.joining(", "));
  }
}

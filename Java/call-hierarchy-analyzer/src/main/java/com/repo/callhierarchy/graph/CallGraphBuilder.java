package com.repo.callhierarchy.graph;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.repo.callhierarchy.index.ProjectIndex;
import com.repo.callhierarchy.model.CallEdge;
import com.repo.callhierarchy.model.CallNode;
import com.repo.callhierarchy.model.ClassRef;
import com.repo.callhierarchy.model.LeafReason;
import com.repo.callhierarchy.model.MethodRef;
import com.repo.callhierarchy.model.Origin;
import com.repo.callhierarchy.model.UnresolvedCall;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CallGraphBuilder {
  public record BuildResult(
      CallNode root,
      List<CallEdge> edges,
      List<UnresolvedCall> unresolved,
      List<String> cycles,
      List<ClassRef> classes) {}

  private final ProjectIndex index;

  public CallGraphBuilder(ProjectIndex index) {
    this.index = index;
  }

  public BuildResult build(MethodRef entryRef, MethodDeclaration entryMd) {
    List<CallEdge> edges = new ArrayList<>();
    List<UnresolvedCall> unresolved = new ArrayList<>();
    List<String> cycles = new ArrayList<>();
    Map<String, ClassRef> classes = new LinkedHashMap<>();
    Set<String> pathStack = new LinkedHashSet<>();

    putClass(classes, entryRef, 0, "entry");

    CallNode root =
        expand(entryRef, entryMd, 0, entryRef.shortLabel(), pathStack, edges, unresolved, cycles, classes);

    List<ClassRef> classList = new ArrayList<>(classes.values());
    classList.sort(Comparator.comparing(ClassRef::typeFqn));
    return new BuildResult(root, edges, unresolved, cycles, classList);
  }

  private CallNode expand(
      MethodRef current,
      MethodDeclaration md,
      int depth,
      String path,
      Set<String> pathStack,
      List<CallEdge> edges,
      List<UnresolvedCall> unresolved,
      List<String> cycles,
      Map<String, ClassRef> classes) {

    if (depth >= index.config().maxDepth()) {
      return new CallNode(current, depth, LeafReason.DEPTH_LIMIT, List.of(), path);
    }
    if (!pathStack.add(current.key())) {
      cycles.add(current.key());
      return new CallNode(current, depth, LeafReason.CYCLE, List.of(), path);
    }

    List<CallNode> children = new ArrayList<>();

    List<MethodCallExpr> calls = md.findAll(MethodCallExpr.class);
    for (MethodCallExpr call : calls) {
      handleMethodCall(
          current, call, depth, path, pathStack, edges, unresolved, cycles, classes, children);
    }

    List<ObjectCreationExpr> news = md.findAll(ObjectCreationExpr.class);
    for (ObjectCreationExpr creation : news) {
      handleConstructor(
          current, creation, depth, path, pathStack, edges, unresolved, cycles, classes, children);
    }

    pathStack.remove(current.key());
    LeafReason reason = children.isEmpty() ? LeafReason.SOURCE_END : LeafReason.NONE;
    return new CallNode(current, depth, reason, children, path);
  }

  private void handleMethodCall(
      MethodRef caller,
      MethodCallExpr call,
      int depth,
      String path,
      Set<String> pathStack,
      List<CallEdge> edges,
      List<UnresolvedCall> unresolved,
      List<String> cycles,
      Map<String, ClassRef> classes,
      List<CallNode> children) {
    Integer line = call.getBegin().map(p -> p.line).orElse(null);
    try {
      ResolvedMethodDeclaration resolved = call.resolve();
      String typeFqn = resolved.declaringType().getQualifiedName();
      if (index.config().isExcluded(typeFqn)) {
        return;
      }
      List<String> params = new ArrayList<>();
      for (int i = 0; i < resolved.getNumberOfParams(); i++) {
        params.add(ProjectIndex.describeResolvedType(resolved.getParam(i).getType()));
      }
      Origin origin = index.originOf(typeFqn);
      String jar = index.jarNameOf(typeFqn);
      MethodRef callee =
          new MethodRef(
              typeFqn,
              resolved.getName(),
              params,
              null,
              null,
              null,
              origin,
              jar);

      putClass(classes, callee, depth + 1, "callee");
      edges.add(new CallEdge(caller, callee, CallEdge.Kind.INVOKE, line, depth + 1));

      String childPath = path + " > " + callee.shortLabel();
      children.add(
          expandOrLeaf(
              callee, depth + 1, childPath, pathStack, edges, unresolved, cycles, classes));
    } catch (Exception e) {
      unresolved.add(
          new UnresolvedCall(
              caller.key(),
              call.toString(),
              line,
              e.getClass().getSimpleName() + ": " + safeMsg(e),
              "Add supporting JAR via --jar / --lib-dir / --classpath-file, or ensure sources cover the type"));
    }
  }

  private void handleConstructor(
      MethodRef caller,
      ObjectCreationExpr creation,
      int depth,
      String path,
      Set<String> pathStack,
      List<CallEdge> edges,
      List<UnresolvedCall> unresolved,
      List<String> cycles,
      Map<String, ClassRef> classes,
      List<CallNode> children) {
    Integer line = creation.getBegin().map(p -> p.line).orElse(null);
    try {
      ResolvedConstructorDeclaration resolved = creation.resolve();
      String typeFqn = resolved.declaringType().getQualifiedName();
      if (index.config().isExcluded(typeFqn)) {
        return;
      }
      List<String> params = new ArrayList<>();
      for (int i = 0; i < resolved.getNumberOfParams(); i++) {
        params.add(ProjectIndex.describeResolvedType(resolved.getParam(i).getType()));
      }
      Origin origin = index.originOf(typeFqn);
      MethodRef callee =
          new MethodRef(
              typeFqn, "<init>", params, null, null, null, origin, index.jarNameOf(typeFqn));
      putClass(classes, callee, depth + 1, "callee");
      edges.add(new CallEdge(caller, callee, CallEdge.Kind.NEW, line, depth + 1));
      String childPath = path + " > " + callee.shortLabel();
      children.add(
          expandOrLeaf(
              callee, depth + 1, childPath, pathStack, edges, unresolved, cycles, classes));
    } catch (Exception e) {
      unresolved.add(
          new UnresolvedCall(
              caller.key(),
              creation.toString(),
              line,
              e.getClass().getSimpleName() + ": " + safeMsg(e),
              "Add supporting JAR via --jar / --lib-dir, or include type sources"));
    }
  }

  private CallNode expandOrLeaf(
      MethodRef callee,
      int depth,
      String path,
      Set<String> pathStack,
      List<CallEdge> edges,
      List<UnresolvedCall> unresolved,
      List<String> cycles,
      Map<String, ClassRef> classes) {

    if (callee.origin() == Origin.JAR) {
      return new CallNode(callee, depth, LeafReason.JAR_LEAF, List.of(), path);
    }
    if (callee.origin() == Origin.JDK || index.config().isExcluded(callee.typeFqn())) {
      return new CallNode(callee, depth, LeafReason.FILTERED, List.of(), path);
    }

    Optional<MethodDeclaration> mdOpt = index.findMethod(callee.key());
    if (mdOpt.isEmpty()) {
      // Fallback: name-only within type
      List<MethodDeclaration> byName =
          index.findMethodsByName(callee.typeFqn(), callee.methodName());
      if (byName.size() == 1) {
        mdOpt = Optional.of(byName.get(0));
      } else if ("<init>".equals(callee.methodName())) {
        return new CallNode(callee, depth, LeafReason.SOURCE_END, List.of(), path);
      }
    }

    if (mdOpt.isPresent() && callee.origin() == Origin.SOURCE) {
      MethodRef sourceRef = index.toMethodRef(callee.typeFqn(), mdOpt.get());
      return expand(
          sourceRef, mdOpt.get(), depth, path, pathStack, edges, unresolved, cycles, classes);
    }

    // Resolved but no source body (e.g. UNKNOWN from jar without index hit)
    if (callee.origin() == Origin.UNKNOWN && index.jarNameOf(callee.typeFqn()) != null) {
      MethodRef jarRef = callee.withOrigin(Origin.JAR, index.jarNameOf(callee.typeFqn()));
      return new CallNode(jarRef, depth, LeafReason.JAR_LEAF, List.of(), path);
    }

    return new CallNode(callee, depth, LeafReason.SOURCE_END, List.of(), path);
  }

  private static void putClass(
      Map<String, ClassRef> classes, MethodRef ref, int depth, String role) {
    classes.merge(
        ref.typeFqn(),
        new ClassRef(ref.typeFqn(), ref.origin(), ref.jarName(), depth, role),
        (existing, incoming) -> {
          if (incoming.firstDepth() < existing.firstDepth()) {
            return incoming;
          }
          return existing;
        });
  }

  private static String safeMsg(Exception e) {
    String m = e.getMessage();
    if (m == null) {
      return e.getClass().getSimpleName();
    }
    return m.length() > 300 ? m.substring(0, 300) + "..." : m;
  }
}

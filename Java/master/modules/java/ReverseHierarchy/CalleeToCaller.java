import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.util.*;

public class CalleeToCaller {

    // Map to store the methods and their callers
    private static final Map<String, Set<String>> reverseCallHierarchy = new HashMap<>();

    public static void main(String[] args) throws Exception {
        // Path to the source folder containing Java files
        File sourceDir = new File("src/main/java/com/example");

        // Parse all files in the source directory
        parseDirectory(sourceDir);

        // Print reverse call hierarchy for a specific method
        String targetMethod = "methodC";  // Replace with the method name to analyze
        System.out.println("Callees to Callers for: " + targetMethod);
        printCallHierarchy(targetMethod, new HashSet<>());
    }

    private static void parseDirectory(File folder) throws Exception {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                parseDirectory(file);
            } else if (file.getName().endsWith(".java")) {
                CompilationUnit cu = StaticJavaParser.parse(file);
                cu.accept(new MethodVisitor(), null);
            }
        }
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration md, Void arg) {
            String methodName = md.getNameAsString();
            md.accept(new MethodCallVisitor(methodName), null);
            super.visit(md, arg);
        }
    }

    private static class MethodCallVisitor extends VoidVisitorAdapter<Void> {
        private final String callerMethod;

        public MethodCallVisitor(String callerMethod) {
            this.callerMethod = callerMethod;
        }

        @Override
        public void visit(MethodCallExpr mc, Void arg) {
            String calledMethod = mc.getNameAsString();
            reverseCallHierarchy
                    .computeIfAbsent(calledMethod, k -> new HashSet<>())
                    .add(callerMethod);
            super.visit(mc, arg);
        }
    }

    private static void printCallHierarchy(String methodName, Set<String> visited) {
        if (visited.contains(methodName)) return; // Prevent infinite loops
        visited.add(methodName);

        Set<String> callers = reverseCallHierarchy.get(methodName);
        if (callers != null) {
            for (String caller : callers) {
                System.out.println(methodName + " is called by " + caller);
                printCallHierarchy(caller, visited); // Recursively trace back
            }
        }
    }
}

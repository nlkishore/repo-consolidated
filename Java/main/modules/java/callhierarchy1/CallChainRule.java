import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;

public class CallChainRule extends AbstractJavaRule {

    // Map to store the call chain
    private final Map<String, Set<String>> callChain = new HashMap<>();

    // Class loader to resolve external classes
    private ClassLoader externalClassLoader;

    public CallChainRule() {
        try {
            // Initialize class loader with external JAR files
            externalClassLoader = new URLClassLoader(new URL[]{
                new URL("file:///path/to/external.jar")
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        String className = node.getImage();

        // Process all methods in the class
        List<ASTMethodDeclaration> methods = node.findDescendantsOfType(ASTMethodDeclaration.class);
        for (ASTMethodDeclaration method : methods) {
            String methodName = className + "." + method.getName();
            resolveMethodCalls(method, methodName);
        }

        return super.visit(node, data);
    }

    private void resolveMethodCalls(ASTMethodDeclaration method, String caller) {
        // Find all method calls in the current method
        List<ASTMethodCall> methodCalls = method.findDescendantsOfType(ASTMethodCall.class);
        for (ASTMethodCall methodCall : methodCalls) {
            String callee = methodCall.getMethodName();
            if (callee != null) {
                callChain.computeIfAbsent(caller, k -> new HashSet<>()).add(callee);

                // Attempt to resolve external methods if applicable
                resolveExternalMethodCall(caller, methodCall);
            }
        }
    }

    private void resolveExternalMethodCall(String caller, ASTMethodCall methodCall) {
        String className = methodCall.getQualifier() != null ? methodCall.getQualifier().getImage() : null;

        if (className != null) {
            try {
                // Load external class
                Class<?> clazz = externalClassLoader.loadClass(className);
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(methodCall.getMethodName())) {
                        // Add to call chain
                        String callee = className + "." + method.getName();
                        callChain.computeIfAbsent(caller, k -> new HashSet<>()).add(callee);

                        // Optionally recurse into the external method's body if available
                        // This requires additional bytecode analysis using ASM/BCEL.
                    }
                }
            } catch (ClassNotFoundException e) {
                // Log or handle the missing class
                System.err.println("Class not found: " + className);
            }
        }
    }

    @Override
    public void end(RuleContext ctx) {
        // Print the full call chain
        for (Map.Entry<String, Set<String>> entry : callChain.entrySet()) {
            StringBuilder output = new StringBuilder(entry.getKey() + " calls: ");
            output.append(String.join(", ", entry.getValue()));
            ctx.getReport().addRuleViolation(
                createViolation(ctx, 0, output.toString())
            );
        }

        super.end(ctx);
    }
}

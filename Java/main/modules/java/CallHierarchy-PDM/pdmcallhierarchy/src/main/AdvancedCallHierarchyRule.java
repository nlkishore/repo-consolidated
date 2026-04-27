import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.RuleContext;

import java.util.List;
import java.util.stream.Collectors;

public class AdvancedCallHierarchyRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        RuleContext context = (RuleContext) data;

        // Collect all method calls using streams (Java 8 feature)
        List<ASTMethodCall> methodCalls = node.findDescendantsOfType(ASTMethodCall.class)
                .stream()
                .collect(Collectors.toList());

        methodCalls.forEach(methodCall -> {
            // Logic to process method calls and fetch external dependencies
            String methodName = methodCall.getMethodName();
            System.out.println("Method call: " + methodName);

            // Assuming your external JAR provides some utility methods
            ExternalLibraryUtil.processMethodCall(methodName);
        });

        return super.visit(node, data);
    }
}

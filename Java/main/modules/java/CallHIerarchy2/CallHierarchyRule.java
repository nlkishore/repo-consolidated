import net.sourceforge.pmd.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.properties.StringProperty;

public class CallHierarchyRule extends AbstractJavaRule {
    private static final StringProperty METHOD_NAME = new StringProperty("methodName", "Method name to check", "", 1.0f);

    public CallHierarchyRule() {
        definePropertyDescriptor(METHOD_NAME);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        String methodName = getProperty(METHOD_NAME);
        if (node.getMethodName().equals(methodName)) {
            Report report = (Report) data;
            report.addViolation(this, node);
        }
        return super.visit(node, data);
    }
}

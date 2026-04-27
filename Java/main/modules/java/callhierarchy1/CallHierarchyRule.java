//package com.example;

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.RuleContext;

public class CallHierarchyRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {
        RuleContext context = (RuleContext) data;

        if (node.jjtGetNumChildren() > 0) {
            String methodName = node.jjtGetChild(0).getImage();
            if (methodName != null) {
                System.out.println("Method call: " + methodName);

                // Process method call using an external library
                ExternalLibraryUtil.processMethodCall(methodName);
            }
        }

        return super.visit(node, data);
    }
}

package com.uob.callhierarchy;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CallHierarchyExample {

    public static void main(String[] args) throws IOException {
        File file = new File("path/to/YourClass.java");
        FileInputStream in = new FileInputStream(file);
        
        // Parse the file
        ParseResult<CompilationUnit> parseResult = null;
		try {
			parseResult = new JavaParser().parse(in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Handle the ParseResult
        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            CompilationUnit cu = parseResult.getResult().get();
            
            MethodCallVisitor methodCallVisitor = new MethodCallVisitor();
            cu.accept(methodCallVisitor, null);

            methodCallVisitor.methodCalls.forEach(call -> {
                call.getBegin().ifPresent(location -> 
                    System.out.println("Method called at: " + location.line + ":" + location.column)
                );
            });
        } else {
            System.out.println("Parsing failed.");
        }
    }

    private static class MethodCallVisitor extends VoidVisitorAdapter<Void> {
        Set<MethodCallExpr> methodCalls = new HashSet<>();

        @Override
        public void visit(MethodCallExpr methodCall, Void arg) {
            super.visit(methodCall, arg);
            methodCalls.add(methodCall);
        }
    }
}

package com.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CallHierarchyExample {

    public static void main(String[] args) throws IOException {
        Map<String, Set<String>> callGraph = new HashMap<>();
        
        // Parse each file in the project
        parseFile("src/main/java/com/example/OrderService.java", callGraph);
        parseFile("src/main/java/com/example/CustomerService.java", callGraph);
        parseFile("src/main/java/com/example/InvoiceService.java", callGraph);

        // Print the call hierarchy
        printCallHierarchy("calculateTotal", callGraph, new HashSet<>());
    }

    private static void parseFile(String filePath, Map<String, Set<String>> callGraph) throws IOException {
        FileInputStream in = new FileInputStream(filePath);
        
        // Parse the file
        ParseResult<CompilationUnit> parseResult = JavaParser.parse(in);
        
        // Handle the ParseResult
        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            CompilationUnit cu = parseResult.getResult().get();
            
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration md, Void arg) {
                    String methodName = md.getNameAsString();
                    Set<String> calledMethods = new HashSet<>();

                    md.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(MethodCallExpr mce, Void arg) {
                            calledMethods.add(mce.getNameAsString());
                            super.visit(mce, arg);
                        }
                    }, null);

                    callGraph.put(methodName, calledMethods);
                    super.visit(md, arg);
                }
            }, null);
        } else {
            System.out.println("Parsing failed for file: " + filePath);
        }
    }

    private static void printCallHierarchy(String methodName, Map<String, Set<String>> callGraph, Set<String> visited) {
        if (!visited.add(methodName)) {
            return; // Avoid circular references
        }

        System.out.println(methodName);
        Set<String> calledMethods = callGraph.get(methodName);

        if (calledMethods != null) {
            for (String calledMethod : calledMethods) {
                printCallHierarchy(calledMethod, callGraph, visited);
            }
        }
    }
}

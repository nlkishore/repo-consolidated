package com.uob.testcase;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
mport org.eclipse.jdt.core.dom.*;

public class JUnitGenerator {

    public static void main(String[] args) {
        String sourcePath = "C:/Users/nlaxm/IdeaProjects/Hibernate/src/main/java/org/example"; // Source folder path
        String outputPath = "C:/Users/nlaxm/IdeaProjects/Hibernate/src/test/java/com/example"; // Test folder path
        
        File sourceFolder = new File(sourcePath);
        File[] javaFiles = sourceFolder.listFiles((dir, name) -> name.endsWith(".java"));
        
        if (javaFiles != null) {
            for (File javaFile : javaFiles) {
                generateJUnitTest(javaFile, outputPath);
            }
        }
    }

    public static void generateJUnitTest(File javaFile, String outputPath) {
        try {
            // Read source file
            String sourceCode = new String(java.nio.file.Files.readAllBytes(javaFile.toPath()));
            
            // Parse source file
            ASTParser parser = ASTParser.newParser(AST.JLS17);
            parser.setSource(sourceCode.toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            
            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            
            // Extract class and method information
            List<String> testMethods = new ArrayList<>();
            String className = javaFile.getName().replace(".java", "");
            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration node) {
                    if (node.isInterface()) return false; // Skip interfaces
                    return super.visit(node);
                }

                @Override
                public boolean visit(MethodDeclaration node) {
                    if (!node.isConstructor() && Modifier.isPublic(node.getModifiers())) {
                        String methodName = node.getName().getIdentifier();
                        testMethods.add(methodName);
                    }
                    return super.visit(node);
                }
            });
            
            // Generate JUnit test code
            String testClassName = className + "Test";
            StringBuilder testClassContent = new StringBuilder();
            testClassContent.append("import org.junit.jupiter.api.Test;\n");
            testClassContent.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
            testClassContent.append("public class ").append(testClassName).append(" {\n\n");

            for (String method : testMethods) {
                testClassContent.append("    @Test\n");
                testClassContent.append("    public void ").append(method).append("_Test() {\n");
                testClassContent.append("        // TODO: Implement test for ").append(method).append("\n");
                testClassContent.append("        fail(\"Not yet implemented\");\n");
                testClassContent.append("    }\n\n");
            }
            testClassContent.append("}\n");
            
            // Write to test file
            File testFile = new File(outputPath, testClassName + ".java");
            testFile.getParentFile().mkdirs(); // Ensure directories exist
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write(testClassContent.toString());
            }

            System.out.println("Generated: " + testFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

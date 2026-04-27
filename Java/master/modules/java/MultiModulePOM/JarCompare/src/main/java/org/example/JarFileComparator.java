package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JarFileComparator {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide two directories to compare.");
            return;
        }

        Path dirPath1 = Paths.get(args[0]);
        Path dirPath2 = Paths.get(args[1]);

        List<File> files1 = getAllFiles(dirPath1.toFile());
        List<File> files2 = getAllFiles(dirPath2.toFile());

        for (File file1 : files1) {
            Path relativePath = dirPath1.relativize(file1.toPath());
            File file2 = dirPath2.resolve(relativePath).toFile();

            if (file2.exists()) {
                compareFiles(file1, file2);
            } else {
                System.out.println("File " + relativePath + " is missing in " + dirPath2);
            }
        }

        for (File file2 : files2) {
            Path relativePath = dirPath2.relativize(file2.toPath());
            File file1 = dirPath1.resolve(relativePath).toFile();

            if (!file1.exists()) {
                System.out.println("File " + relativePath + " is missing in " + dirPath1);
            }
        }
    }

    private static List<File> getAllFiles(File dir) {
        List<File> files = new ArrayList<>();
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    files.addAll(getAllFiles(file));
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

    private static void compareFiles(File file1, File file2) {
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(file1));
            BufferedReader reader2 = new BufferedReader(new FileReader(file2));

            String line1 = reader1.readLine();
            String line2 = reader2.readLine();
            int lineNumber = 1;

            while (line1 != null || line2 != null) {
                if (line1 == null || line2 == null || !line1.equals(line2)) {
                    System.out.println("Difference at line " + lineNumber + " in file " + file1.getName());
                    System.out.println("File 1: " + (line1 != null ? line1 : "EOF"));
                    System.out.println("File 2: " + (line2 != null ? line2 : "EOF"));
                }

                line1 = reader1.readLine();
                line2 = reader2.readLine();
                lineNumber++;
            }

            reader1.close();
            reader2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JarFileDiffComparator {

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
            ProcessBuilder builder = new ProcessBuilder("diff", "-w", file1.getAbsolutePath(), file2.getAbsolutePath());
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Difference found: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Files " + file1.getName() + " and " + file2.getName() + " are identical (ignoring spaces and tabs).");
            } else {
                System.out.println("Files " + file1.getName() + " and " + file2.getName() + " differ (ignoring spaces and tabs).");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


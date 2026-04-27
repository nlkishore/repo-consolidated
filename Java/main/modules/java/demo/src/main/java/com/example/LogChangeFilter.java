package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class LogChangeFilter {

    public static void main(String[] args) {
        // Path to the diff list file
        String diffListFilePath = "path/to/diff_list.txt";
        // Read the diff list
        List<String> diffList = readDiffList(diffListFilePath);
        // Process each file in the diff list
        List<String> logChangedFiles = diffList.stream()
                .filter(LogChangeFilter::isLogRelatedChange)
                .collect(Collectors.toList());

        // Output the result
        logChangedFiles.forEach(System.out::println);
    }

    private static List<String> readDiffList(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static boolean isLogRelatedChange(String filePath) {
        try {
            // Read the file content
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            // Check for logging-related changes
            for (String line : lines) {
                if (isLogLine(line)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isLogLine(String line) {
        // Define logging-related patterns (add more patterns as needed)
        String[] logPatterns = {
                "Logger", // Common logger class
                "log\\.", // Log method calls (e.g., log.info, log.debug)
                "import\\s+org\\.slf4j\\.", // SLF4J imports
                "import\\s+java\\.util\\.logging\\.", // java.util.logging imports
                "import\\s+org\\.apache\\.log4j\\." // Log4j imports
        };

        for (String pattern : logPatterns) {
            if (line.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}


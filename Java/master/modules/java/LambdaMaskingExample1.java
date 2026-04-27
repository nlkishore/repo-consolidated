import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example  of masking Numbers and email in file using lamda , 
 * passing lamda expression as argument only mask whole numbers  , 
 * if number length greater 5 , mask characters lenght minus 5 than excluding float and doube  numbers
 */

public class LambdaMaskingExample1 {
    public static void main(String[] args) {
        String inputFile = "input.txt"; // Path to input file
        String outputFile = "output.txt"; // Path to output file

        try {
            maskData(inputFile, outputFile, 
                line -> maskNumbers(line), 
                line -> maskEmails(line));
            System.out.println("Data masking completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void maskData(String inputFile, String outputFile, 
                                Function<String, String> numberMaskingFunction, 
                                Function<String, String> emailMaskingFunction) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        String line;

        while ((line = reader.readLine()) != null) {
            // Mask numbers
            line = numberMaskingFunction.apply(line);

            // Mask email addresses
            line = emailMaskingFunction.apply(line);

            // Write the masked line to output file
            writer.write(line);
            writer.newLine();
        }

        reader.close();
        writer.close();
    }

   /* public static String maskNumbers(String input) {
        return input.replaceAll("\\b\\d{6,}\\b", 
            match -> {
                String matched = match.group();
                int length = matched.length();
                return matched.substring(0, length - 5) + "*****";
            });
    }*/



    public static String maskNumbers1(String line) {
        // Regular expression to match whole numbers excluding float and double numbers
        String regex = "\\b(?!\\d*\\.\\d+\\b)\\d{6,}\\b";

        // Create a pattern object
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher object
        Matcher matcher = pattern.matcher(line);

        // StringBuffer to store the modified line
        StringBuffer result = new StringBuffer();

        // Iterate over matches and apply masking
        while (matcher.find()) {
            String matched = matcher.group();
            // Mask the number
            String masked = matched.substring(0, matched.length() - 5) + "*****";
            // Append the masked number to the result
            matcher.appendReplacement(result, masked);
        }

        // Append the rest of the line
        matcher.appendTail(result);

        return result.toString();
    }

    public static String maskEmails(String input) {
        // Regular expression to match email addresses
        String regex = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Mask the email address
            matcher.appendReplacement(result, "*****@*****.***");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
}





import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMaskingExample {
    public static void main(String[] args) {
        String inputFile = "/Users/yaswitha/MaskingData/input.txt"; // Path to input file
        String outputFile = "/Users/yaswitha/MaskingData/output.txt"; // Path to output file

        try {
            maskData(inputFile, outputFile);
            System.out.println("Data masking completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void maskData(String inputFile, String outputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        String line;

        while ((line = reader.readLine()) != null) {
            // Mask numbers greater than 5 digits
            line = maskNumbers(line);

            // Mask email addresses
            line = maskEmails(line);

            // Write the masked line to output file
            writer.write(line);
            writer.newLine();
        }

        reader.close();
        writer.close();
    }

    public static String maskNumbers(String number) {
        // Regular expression to match numbers greater than 5 digits
        String regex = "\\b\\d{6,}\\b";
        return number.replaceAll(regex, "*****");
    
       

        //return maskingLogic.apply(number);

    }
    

    Function<Number, String> maskingLogic = num -> {
        String numberString = num.toString();
        if (num instanceof Integer || num instanceof Long) {
            // Whole number
            return maskWholeNumber(numberString);
        } else if (num instanceof Double || num instanceof Float) {
            // Floating point number
            return maskFloatingPointNumber(numberString);
        } else {
            // Unknown type
            return "Unknown type";
        }
    };

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


    public static String maskWholeNumber(String numberString) {
        // Mask the whole number
        return numberString.replaceAll("\\d", "*");
    }

    public static String maskFloatingPointNumber(String numberString) {
        // Mask the floating point number
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(numberString);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String matched = matcher.group();
            String masked = maskWholeNumber(matched);
            matcher.appendReplacement(result, masked);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}

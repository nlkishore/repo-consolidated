import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Lambda expression to replaceall numbers matching pattern in multiple occurances

 */
public class LambdaReplaceAllExample {
    public static void main(String[] args) {
        String input = "There are 123 apples and 456 oranges on the table.";

        // Define lambda expression for replacing numbers
        Function<String, String> replaceNumbers = line -> {
            // Regular expression to match numbers
            String regex = "\\b\\d+\\b";

            // Create a pattern object
            Pattern pattern = Pattern.compile(regex);

            // Create a matcher object
            Matcher matcher = pattern.matcher(line);

            // Replace all numbers with "X"
            return matcher.replaceAll("X");
        };

        // Apply the lambda expression to the input string
        String result = replaceNumbers.apply(input);

        // Output the result
        System.out.println("Original: " + input);
        System.out.println("Modified: " + result);
    }
}


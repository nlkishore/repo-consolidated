import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceAllWithLog4JSupport {
    private static final Logger logger = LogManager.getLogger(ReplaceAllWithLog4JSupport.class);

    public static void main(String[] args) {
        String input = "There are 123 apples and 456 oranges on the table.";
        logger.debug(" Argument Input " + input);
        // Define lambda expression for replacing numbers
        Function<String, String> replaceNumbers = line -> {
            // Regular expression to match numbers
            String regex = "\\b\\d+\\b";
            // Create a pattern object
            Pattern pattern = Pattern.compile(regex);
            // Create a matcher object
            Matcher matcher = pattern.matcher(line);
            // Replace all numbers with "X"
            String replacedString = matcher.replaceAll("X");
            // Log the replaced string
            logger.info("Original: " + line);
            logger.info("Modified: " + replacedString);
            return replacedString;
        };

        // Apply the lambda expression to the input string
        String result = replaceNumbers.apply(input);
    }
}

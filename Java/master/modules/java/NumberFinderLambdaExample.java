import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
//import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberFinderLambdaExample {
    public static void main(String[] args) {
        String line = "There are 3 apples and 2 oranges on the table.";

        List<String> numbersFound = findNumbers(line);

        System.out.println("Numbers found in the line:");
        numbersFound.forEach(System.out::println);
    }

    public static List<String> findNumbers(String line) {
        Pattern pattern = Pattern.compile("\\b\\d+\\b");
        Matcher matcher = pattern.matcher(line);

        Predicate<String> isNumber = s -> {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        };

        return matcher.results()
                .map(MatchResult::group)
                .filter(isNumber)
                .toList();
    }
}


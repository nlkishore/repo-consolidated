import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class LambdaExpressionExample {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        // Pass lambda expressions as arguments
        processNumbers(numbers, x -> x * 2, x -> x > 4);
    }

    public static void processNumbers(List<Integer> numbers, Function<Integer, Integer> operation,
                                      Predicate<Integer> condition) {
        System.out.println("Performing operations on numbers:");

        for (Integer number : numbers) {
            // Apply the operation if it satisfies the condition
            if (condition.test(number)) {
                int result = operation.apply(number);
                System.out.println("Original: " + number + ", Operation Result: " + result);
            }
        }
    }
}


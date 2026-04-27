import java.util.function.Function;

public class LambdaAsArgumentExample {
    public static void main(String[] args) {
        Function<Integer, Integer> addOne = x -> x + 1;
        Function<Integer, Integer> multiplyByTwo = x -> x * 2;

        // Passing a lambda expression as an argument to another lambda expression
        Function<Integer, Integer> composedFunction = compose(addOne, multiplyByTwo);

        // Testing the composed function
        System.out.println("Result: " + composedFunction.apply(5)); // Output: Result: 12
    }

    public static Function<Integer, Integer> compose(Function<Integer, Integer> f1, Function<Integer, Integer> f2) {
        // Compose two functions: f1(f2(x))
        return x -> f1.apply(f2.apply(x));
    }
}


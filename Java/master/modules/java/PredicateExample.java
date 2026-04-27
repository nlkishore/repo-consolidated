import java.util.function.Predicate;

public class PredicateExample {
    public static void main(String[] args) {
        Predicate<Integer> isPositive = num -> num > 0;

        System.out.println("Is 5 positive? " + isPositive.test(5));
        System.out.println("Is -5 positive? " + isPositive.test(-5));
    }
}


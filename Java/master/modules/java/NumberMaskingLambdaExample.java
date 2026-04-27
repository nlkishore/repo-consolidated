import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberMaskingLambdaExample {
    public static void main(String[] args) {
        Object[] data = {10, 10.5, 10.0f, 100L, "not a number"};

        for (Object obj : data) {
            if (obj instanceof Number) {
                String maskedNumber = maskNumber((Number) obj);
                System.out.println(obj + " (masked): " + maskedNumber);
            } else {
                System.out.println(obj + " is not a number.");
            }
        }
    }

    public static String maskNumber(Number number) {
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

        return maskingLogic.apply(number);
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


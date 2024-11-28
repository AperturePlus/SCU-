import java.util.List;
import java.util.Stack;

public class PostfixEvaluator {
    public static double evaluate(List<String> postfix) {
        Stack<Double> stack = new Stack<>();

        for (String token : postfix) {
            if (isNumber(token)) {
                //push number
                stack.push(Double.parseDouble(token));
            } else if (isFunction(token)) {
                //apply function, expandable
                double operand = stack.pop();
                double result = applyFunction(token, operand);
                stack.push(result);
            } else if (isOperator(token)) {
                double right = stack.pop();
                if(stack.isEmpty()){
                    stack.push(0.0);
                }
                double left = stack.pop();
                double result = applyOperator(token, left, right);
                stack.push(result);
            }

        }

        return stack.pop();
    }

    private static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFunction(String token) {
        return token.equals("sqrt");
    }

    private static boolean isOperator(String token) {
        return token.matches("[-+*/%^&]");
    }

    private static double applyFunction(String function, double operand) {
        switch (function) {
            case "sqrt":
                return Math.sqrt(operand);
            default:
                throw new IllegalArgumentException("Unsupported function: " + function);
        }
    }

    private static double applyOperator(String operator, double left, double right) {
        return switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            case "%" -> left % right;
            case "^" -> Math.pow(left, right);
            case "&" -> Math.pow(left, 1.0 / right);
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }


}

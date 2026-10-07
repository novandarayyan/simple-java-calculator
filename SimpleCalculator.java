import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimpleCalculator {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("Simple Calculator");
        System.out.println("Supported operators: +, -, x, :, /, % and parentheses ()");
        System.out.print("Enter expression: ");

        String expression = input.nextLine().trim();

        if (expression.isEmpty()) {
            System.out.println("No expression entered.");
            return;
        }

        try {
            ExpressionParser parser = new ExpressionParser(expression);
            double result = parser.parse();
            System.out.println("Result: " + result);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class ExpressionParser {
    private final List<Token> tokens;
    private int index = 0;

    public ExpressionParser(String expression) {
        this.tokens = tokenize(expression);
    }

    public double parse() {
        double result = parseExpression();

        if (index != tokens.size()) {
            throw new IllegalArgumentException("Unexpected token: " + tokens.get(index).value);
        }

        return result;
    }

    private double parseExpression() {
        double value = parseTerm();

        while (index < tokens.size()) {
            Token token = tokens.get(index);
            if (token.type == TokenType.PLUS) {
                index++;
                value += parseTerm();
            } else if (token.type == TokenType.MINUS) {
                index++;
                value -= parseTerm();
            } else {
                break;
            }
        }

        return value;
    }

    private double parseTerm() {
        double value = parseFactor();

        while (index < tokens.size()) {
            Token token = tokens.get(index);
            if (token.type == TokenType.MULTIPLY || token.type == TokenType.DIVIDE) {
                index++;
                double right = parseFactor();

                if (token.type == TokenType.MULTIPLY) {
                    value *= right;
                } else {
                    if (right == 0) {
                        throw new IllegalArgumentException("Division by zero is not allowed.");
                    }
                    value /= right;
                }
            } else {
                break;
            }
        }

        return value;
    }

    private double parseFactor() {
        if (index >= tokens.size()) {
            throw new IllegalArgumentException("Missing number or bracket.");
        }

        Token token = tokens.get(index);
        double value;

        if (token.type == TokenType.PLUS) {
            index++;
            return parseFactor();
        }

        if (token.type == TokenType.MINUS) {
            index++;
            return -parseFactor();
        }

        if (token.type == TokenType.NUMBER) {
            index++;
            value = token.numericValue;
        } else if (token.type == TokenType.LEFT_PAREN) {
            index++;
            value = parseExpression();
            if (index >= tokens.size() || tokens.get(index).type != TokenType.RIGHT_PAREN) {
                throw new IllegalArgumentException("Missing closing bracket.");
            }
            index++;
        } else {
            throw new IllegalArgumentException("Unexpected token: " + token.value);
        }

        while (index < tokens.size() && tokens.get(index).type == TokenType.PERCENT) {
            index++;
            value = value / 100.0;
        }

        return value;
    }

    private static List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (Character.isWhitespace(ch)) {
                continue;
            }

            if (Character.isDigit(ch) || ch == '.') {
                currentNumber.append(ch);
                continue;
            }

            if (currentNumber.length() > 0) {
                tokens.add(new Token(TokenType.NUMBER, currentNumber.toString(), Double.parseDouble(currentNumber.toString())));
                currentNumber.setLength(0);
            }

            switch (ch) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+"));
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-"));
                    break;
                case 'x':
                case 'X':
                case '*':
                    tokens.add(new Token(TokenType.MULTIPLY, String.valueOf(ch)));
                    break;
                case ':':
                case '/':
                    tokens.add(new Token(TokenType.DIVIDE, String.valueOf(ch)));
                    break;
                case '%':
                    tokens.add(new Token(TokenType.PERCENT, "%"));
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported character: " + ch);
            }
        }

        if (currentNumber.length() > 0) {
            tokens.add(new Token(TokenType.NUMBER, currentNumber.toString(), Double.parseDouble(currentNumber.toString())));
        }

        return tokens;
    }
}

enum TokenType {
    NUMBER,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    PERCENT,
    LEFT_PAREN,
    RIGHT_PAREN
}

class Token {
    final TokenType type;
    final String value;
    final double numericValue;

    Token(TokenType type, String value) {
        this(type, value, 0);
    }

    Token(TokenType type, String value, double numericValue) {
        this.type = type;
        this.value = value;
        this.numericValue = numericValue;
    }
}

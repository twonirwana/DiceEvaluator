package de.janno.evaluator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract evaluator, able to evaluate infix expressions.
 *
 * @param <T> The type of values handled by the evaluator
 */
public abstract class AbstractEvaluator<T> {
    private final Tokenizer<T> tokenizer;
    private final Parameters<T> parameters;

    /**
     * constructor of the evaluator
     * @param parameters to configure the evaluator
     */
    protected AbstractEvaluator(Parameters<T> parameters) {
        tokenizer = new Tokenizer<>(parameters);
        this.parameters = parameters;
    }

    private static <T> List<T> reverse(Collection<T> collection) {
        List<T> result = new ArrayList<>(collection.size());
        for (T t : collection) {
            result.add(0, t);
        }
        return result;
    }

    private static <T> boolean hasStackTokenPrecedence(Token<T> currentToken, Token<T> stackToken) {
        return stackToken.getOperator().isPresent()
                && ((Operator.Associativity.LEFT.equals(currentToken.getOperatorAssociativity().orElseThrow())
                && (currentToken.getOperatorPrecedence().orElseThrow() <= stackToken.getOperatorPrecedence().orElseThrow())) ||
                (currentToken.getOperatorPrecedence().orElseThrow() < stackToken.getOperatorPrecedence().orElseThrow()));
    }

    private void processTokenToValues(Deque<T> values, Token<T> token) throws ExpressionException {
        if (token.getLiteral().isPresent()) { // If the token is a literal, a constant, or a variable name
            String literal = token.getLiteral().get();
            values.push(toValue(literal));
        } else if (token.getOperator().isPresent()) { // If the token is an operator
            Operator<T> operator = token.getOperator().get();
            int argumentCount = token.getOperatorType().orElseThrow().argumentCount;
            if (values.size() < argumentCount) {
                throw new ExpressionException("Not enough values, %s needs %d but there where only %s".formatted(operator.getNames(), argumentCount, values));
            }
            values.push(operator.evaluate(getArguments(values, argumentCount)));
        } else {
            throw new ExpressionException(token.toString());
        }
    }

    private void doFunction(Deque<T> values, Function<T> function, int argumentCount) throws ExpressionException {
        if (function.getMinArgumentCount() > argumentCount || function.getMaxArgumentCount() < argumentCount || values.size() < argumentCount) {
            throw new ExpressionException("Invalid argument count for %s".formatted(function.getName()));
        }
        values.push(function.evaluate(getArguments(values, argumentCount)));
    }

    private List<T> getArguments(Deque<T> values, int argumentCount) {
        // The arguments are in reverse order on the values stack
        List<T> result = new ArrayList<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            result.add(0, values.pop());
        }
        return result;
    }

    /**
     * Evaluates a literal and converts it to a value
     *
     * @param literal The literal to convert
     * @return The value
     * @throws ExpressionException if the literal can't be converted to a value.
     */
    protected abstract T toValue(String literal) throws ExpressionException;

    private boolean isFunctionOpenBracket(Token<T> token) {
        if (token == null || !token.isOpenBracket()) {
            return false;
        }
        return parameters.getFunctionBrackets().contains(token.getBrackets().orElseThrow());
    }

    /**
     * Evaluates an expression.
     *
     * @param expression The expression to evaluate
     * @return The result of the evaluation. This can be multiple values, if the expression can't be reduced to a single value.
     * @throws ExpressionException if the expression is not correct.
     */
    public List<T> evaluate(String expression) throws ExpressionException {
        final List<Token<T>> tokens = tokenizer.tokenize(expression);
        final Deque<T> values = new ArrayDeque<>(tokens.size()); // values stack
        final Deque<Token<T>> stack = new ArrayDeque<>(tokens.size()); // operators, function and brackets stack
        final Deque<Integer> previousValuesSize = new ArrayDeque<>(tokens.size());
        Optional<Token<T>> previous = Optional.empty();
        for (Token<T> token : tokens) {

            if (previous.flatMap(Token::getFunction).isPresent() && !isFunctionOpenBracket(token)) {
                String functionName = previous.flatMap(Token::getFunction).map(Function::getName).orElseThrow();
                String allowedBrackets = parameters.getFunctionBrackets().stream().map(BracketPair::getOpen).collect(Collectors.joining(" or "));
                throw new ExpressionException("A function, in this case '%s', must be followed a open function bracket: %s".formatted(functionName, allowedBrackets));
            }

            if (token.getBrackets().isPresent() && token.isOpenBracket()) {
                // If the token is a left parenthesis, then push it onto the stack.
                stack.push(token);
                if (previous.flatMap(Token::getFunction).isPresent()) {
                    if (!parameters.getFunctionBrackets().contains(token.getBrackets().get())) {
                        throw new ExpressionException("Invalid bracket after function: %s".formatted(token));
                    }
                } else {
                    if (!parameters.getExpressionBrackets().contains(token.getBrackets().get())) {
                        throw new ExpressionException("Invalid bracket in expression: %s".formatted(token));
                    }
                }
            } else if (token.isCloseBracket()) {
                if (previous.isEmpty()) {
                    throw new ExpressionException("expression can't start with a close bracket");
                }
                if (previous.map(Token::isSeparator).get()) {
                    throw new ExpressionException("argument is missing");
                }
                BracketPair brackets = token.getBrackets().get();
                // If the token is a right parenthesis:
                boolean openBracketFound = false;
                // Until the token at the top of the stack is a left parenthesis,
                // pop operators off the stack onto the output queue

                while (!stack.isEmpty() && !openBracketFound) {
                    Token<T> stackToken = stack.pop();
                    if (stackToken.getBrackets().isPresent() && stackToken.isOpenBracket()) {
                        if (stackToken.getBrackets().get().equals(brackets)) {
                            openBracketFound = true;
                        } else {
                            throw new ExpressionException("Invalid parenthesis match %s%s".formatted(stackToken.getBrackets().get().getOpen(), brackets.getClose()));
                        }
                    } else {
                        processTokenToValues(values, stackToken);
                    }
                }
                if (!openBracketFound) {
                    // If the stack runs out without finding a left parenthesis, then
                    // there are mismatched parentheses.
                    throw new ExpressionException("Parentheses mismatched");
                }
                if (!stack.isEmpty() && stack.peek().getFunction().isPresent()) {
                    // If the token at the top of the stack is a function token, pop it
                    // onto the output queue.
                    int argumentCount = values.size() - previousValuesSize.pop();
                    doFunction(values, stack.pop().getFunction().orElseThrow(), argumentCount);
                }
            } else if (token.isSeparator()) {
                if (previous.isEmpty()) {
                    throw new ExpressionException("expression can't start with a separator");
                }
                // Verify that there was an argument before this separator
                if (previous.get().isOpenBracket() || previous.get().isSeparator()) {
                    throw new ExpressionException("A separator can't be followed by another separator or open bracket");
                }
                boolean openBracketOnStackReached = false;
                while (!stack.isEmpty() && !openBracketOnStackReached) {
                    if (stack.peek().isOpenBracket()) {
                        openBracketOnStackReached = true;
                    } else {
                        // Until the token at the top of the stack is a left parenthesis,
                        // pop operators off the stack onto the output queue.
                        processTokenToValues(values, stack.pop());
                    }
                }
                if (openBracketOnStackReached) {
                    stack.push(stack.pop());
                }
            } else if (token.getFunction().isPresent()) {
                // If the token is a function token, then push it onto the stack.
                stack.push(token);
                previousValuesSize.push(values.size());
            } else if (token.getOperator().isPresent()) {
                // If the token is an operator, op1, then:
                while (!stack.isEmpty() && hasStackTokenPrecedence(token, stack.peek())) {
                    // While there is an operator token, o2, at the top of the stack
                    // op1 is left-associative and its precedence is less than or equal
                    // to that of op2,
                    // or op1 has precedence less than that of op2,
                    // Let + and ^ be right associative.
                    // Correct transformation from 1^2+3 is 12^3+
                    // The differing operator priority decides pop / push
                    // If 2 operators have equal priority then associativity decides.
                    // Pop o2 off the stack, onto the output queue;
                    processTokenToValues(values, stack.pop());
                }
                stack.push(token);
            } else {
                // If the token is literal then add its value to the output queue.
                if (previous.flatMap(Token::getLiteral).isPresent()) {
                    throw new ExpressionException("There need to be an operator or a separator between two values");
                }
                processTokenToValues(values, token);
            }
            previous = Optional.of(token);
        }
        // When there are no more tokens to read:
        // While there are still operator tokens in the stack:
        for (Token<T> stackToken : stack) {
            if (stackToken.isOpenBracket() || stackToken.isCloseBracket()) {
                throw new ExpressionException("Parentheses mismatched");
            }
            processTokenToValues(values, stackToken);
        }

        return reverse(values);
    }
}

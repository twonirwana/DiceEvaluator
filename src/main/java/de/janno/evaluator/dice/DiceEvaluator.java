package de.janno.evaluator.dice;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import de.janno.evaluator.dice.function.Double;
import de.janno.evaluator.dice.function.*;
import de.janno.evaluator.dice.operator.die.ExplodingAddDice;
import de.janno.evaluator.dice.operator.die.ExplodingDice;
import de.janno.evaluator.dice.operator.die.RegularDice;
import de.janno.evaluator.dice.operator.list.*;
import de.janno.evaluator.dice.operator.math.Appending;
import de.janno.evaluator.dice.operator.math.Divide;
import de.janno.evaluator.dice.operator.math.Multiply;
import de.janno.evaluator.dice.operator.math.NegateOrNegativAppending;
import de.janno.evaluator.dice.random.NumberSupplier;
import de.janno.evaluator.dice.random.RandomNumberSupplier;
import lombok.NonNull;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiceEvaluator {

    private static final int DEFAULT_MAX_NUMBER_OF_DICE = 1000;

    private static final Pattern LIST_REGEX = Pattern.compile("(.+(/.+)+)");
    private final Tokenizer tokenizer;
    private final Parameters parameters;

    public DiceEvaluator() {
        this(new RandomNumberSupplier(), DEFAULT_MAX_NUMBER_OF_DICE);
    }

    public DiceEvaluator(@NonNull NumberSupplier numberSupplier, int maxNumberOfDice) {
        parameters = Parameters.builder()
                .expressionBracket(BracketPair.PARENTHESES)
                .functionBracket(BracketPair.PARENTHESES)
                .escapeBracket(BracketPair.APOSTROPHE)
                .escapeBracket(BracketPair.BRACKETS)
                .operators(ImmutableList.<Operator>builder()
                        .add(new RegularDice(numberSupplier, maxNumberOfDice))
                        .add(new ExplodingDice(numberSupplier, maxNumberOfDice))
                        .add(new ExplodingAddDice(numberSupplier, maxNumberOfDice))
                        .add(new Appending())
                        .add(new Sum())
                        .add(new NegateOrNegativAppending())
                        .add(new Divide())
                        .add(new Multiply())
                        .add(new KeepHighest())
                        .add(new KeepLowest())
                        .add(new GreaterThanFilter())
                        .add(new LesserThanFilter())
                        .add(new GreaterEqualThanFilter())
                        .add(new LesserEqualThanFilter())
                        .add(new Count())
                        .build())
                .functions(ImmutableList.<Function>builder()
                        .add(new Color())
                        .add(new Value())
                        .add(new Concat())
                        .add(new SortAsc())
                        .add(new SortDesc())
                        .add(new Min())
                        .add(new Max())
                        .add(new Cancel())
                        .add(new Double())
                        .add(new IfEqual())
                        .add(new IfGreater())
                        .add(new IfLesser())
                        .add(new GroupCount())
                        .build())
                .separator(",")
                .build();
        tokenizer = new Tokenizer(parameters);

    }

    public static String getHelpText() {
        URL url = Resources.getResource("help.md");
        try {
            return Resources.toString(url, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Roll> reverse(Collection<Roll> collection) {
        List<Roll> result = new ArrayList<>(collection.size());
        for (Roll t : collection) {
            result.add(0, t);
        }
        return result;
    }

    private static boolean hasStackTokenPrecedence(Token currentToken, Token stackToken) {
        return stackToken.getOperator().isPresent()
                && ((Operator.Associativity.LEFT.equals(currentToken.getOperatorAssociativity().orElseThrow())
                && (currentToken.getOperatorPrecedence().orElseThrow() <= stackToken.getOperatorPrecedence().orElseThrow())) ||
                (currentToken.getOperatorPrecedence().orElseThrow() < stackToken.getOperatorPrecedence().orElseThrow()));
    }

    protected @NonNull Roll toValue(@NonNull String literal) {
        Matcher matcher = LIST_REGEX.matcher(literal);
        if (matcher.find()) {
            //Todo is this needed: ('Head'+'Tail') is the same as [Head,Tail] but needs escaping
            List<String> list = Arrays.asList(matcher.group(1).split("/"));
            return new Roll(list.toString(), list.stream()
                    .map(String::trim)
                    .map(s -> new RollElement(s, RollElement.NO_COLOR))
                    .collect(ImmutableList.toImmutableList()), ImmutableList.of(), ImmutableList.of(), null);
        }
        return new Roll(literal, ImmutableList.of(new RollElement(literal, RollElement.NO_COLOR)), ImmutableList.of(), ImmutableList.of(), null);
    }

    private void processTokenToValues(Deque<Roll> values, Token token, Map<String, Roll> constants) throws ExpressionException {
        if (token.getLiteral().isPresent()) { // If the token is a literal, a constant, or a variable name
            String literal = token.getLiteral().get();
            if (constants.containsKey(literal)) {
                values.push(constants.get(literal));
            } else {
                values.push(toValue(literal));
            }
        } else if (token.getOperator().isPresent()) { // If the token is an operator
            Operator operator = token.getOperator().get();
            int argumentCount = token.getOperatorType().orElseThrow().argumentCount;
            if (values.size() < argumentCount) {
                throw new ExpressionException("Not enough values, %s needs %d but there where only %s".formatted(operator.getNames(), argumentCount, values.stream()
                        .map(r -> r.getElements().stream().map(RollElement::toString).collect(Collectors.toList())).collect(Collectors.toList())));
            }
            values.push(operator.evaluate(getArguments(values, argumentCount)));
        } else {
            throw new ExpressionException(token.toString());
        }
    }

    private void doFunction(Deque<Roll> values, Function function, int argumentCount, Map<String, Roll> constantMap) throws ExpressionException {
        if (function.getMinArgumentCount() > argumentCount || function.getMaxArgumentCount() < argumentCount || values.size() < argumentCount) {
            throw new ExpressionException("Invalid argument count for %s".formatted(function.getName()));
        }
        final Roll res = function.evaluate(getArguments(values, argumentCount));
        if (res.getConstantName() != null) {
            constantMap.put(res.getConstantName(), res);
        } else {
            values.push(res);
        }
    }

    private List<Roll> getArguments(Deque<Roll> values, int argumentCount) {
        // The arguments are in reverse order on the values stack
        List<Roll> result = new ArrayList<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            result.add(0, values.pop());
        }
        return result;
    }

    private boolean isFunctionOpenBracket(Token token) {
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
    public List<Roll> evaluate(String expression) throws ExpressionException {
        expression = expression.trim();
        if (Strings.isNullOrEmpty(expression)) {
            return ImmutableList.of(new Roll("", ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), null));
        }

        final List<Token> tokens = tokenizer.tokenize(expression);
        final Deque<Roll> values = new ArrayDeque<>(tokens.size()); // values stack
        final Deque<Token> stack = new ArrayDeque<>(tokens.size()); // operators, function and brackets stack
        final Deque<Integer> previousValuesSize = new ArrayDeque<>(tokens.size());
        final Map<String, Roll> constants = new HashMap<>();
        Optional<Token> previous = Optional.empty();
        for (Token token : tokens) {

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
                    Token stackToken = stack.pop();
                    if (stackToken.getBrackets().isPresent() && stackToken.isOpenBracket()) {
                        if (stackToken.getBrackets().get().equals(brackets)) {
                            openBracketFound = true;
                        } else {
                            throw new ExpressionException("Invalid parenthesis match %s%s".formatted(stackToken.getBrackets().get().getOpen(), brackets.getClose()));
                        }
                    } else {
                        processTokenToValues(values, stackToken, constants);
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
                    doFunction(values, stack.pop().getFunction().orElseThrow(), argumentCount, constants);
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
                        processTokenToValues(values, stack.pop(), constants);
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
                    processTokenToValues(values, stack.pop(), constants);
                }
                stack.push(token);
            } else {
                // If the token is literal then add its value to the output queue.
                if (previous.flatMap(Token::getLiteral).isPresent()) {
                    throw new ExpressionException("There need to be an operator or a separator between two values");
                }
                processTokenToValues(values, token, constants);
            }
            previous = Optional.of(token);
        }
        // When there are no more tokens to read:
        // While there are still operator tokens in the stack:
        for (Token stackToken : stack) {
            if (stackToken.isOpenBracket() || stackToken.isCloseBracket()) {
                throw new ExpressionException("Parentheses mismatched");
            }
            processTokenToValues(values, stackToken, constants);
        }

        return reverse(values);
    }
}

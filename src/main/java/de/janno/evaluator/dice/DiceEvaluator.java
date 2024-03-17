package de.janno.evaluator.dice;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import de.janno.evaluator.dice.function.Double;
import de.janno.evaluator.dice.function.*;
import de.janno.evaluator.dice.operator.bool.*;
import de.janno.evaluator.dice.operator.die.*;
import de.janno.evaluator.dice.operator.list.*;
import de.janno.evaluator.dice.operator.math.*;
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

    private static final String SEPARATOR = ",";
    private static final String LEGACY_LIST_SEPARATOR = "/";
    private static final Pattern LIST_REGEX = Pattern.compile("(.+([%s%s].+)+)".formatted(SEPARATOR, LEGACY_LIST_SEPARATOR), Pattern.DOTALL); //the brackets are used for the escape and are not part of the literal
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
                        .add(new AddToList())
                        .add(new Concat())
                        .add(new Color())
                        .add(new Tag())
                        .add(new Sum())
                        .add(new Repeat())
                        .add(new RepeatList())
                        .add(new NegateOrNegativAddToList())
                        .add(new IntegerDivide())
                        .add(new DecimalDivide())
                        .add(new Multiply())
                        .add(new Modulo())
                        .add(new KeepHighest())
                        .add(new KeepLowest())
                        .add(new GreaterThanFilter())
                        .add(new LesserThanFilter())
                        .add(new GreaterEqualThanFilter())
                        .add(new LesserEqualThanFilter())
                        .add(new EqualFilter())
                        .add(new Count())
                        .add(new Reroll())
                        .add(new EqualBool())
                        .add(new GreaterBool())
                        .add(new GreaterEqualBool())
                        .add(new LesserBool())
                        .add(new LesserEqualBool())
                        .add(new InBool())
                        .add(new AndBool())
                        .add(new OrBool())
                        .add(new NegateBool())
                        .build())
                .functions(ImmutableList.<Function>builder()
                        .add(new ColorFunction())
                        .add(new Value())
                        .add(new ConcatFunction())
                        .add(new SortAsc())
                        .add(new SortDesc())
                        .add(new Min())
                        .add(new Max())
                        .add(new Cancel())
                        .add(new Double())
                        .add(new IfEqual())
                        .add(new If())
                        .add(new IfGreater())
                        .add(new IfIn())
                        .add(new Replace())
                        .add(new Explode())
                        .add(new IfLesser())
                        .add(new GroupCount())
                        .build())
                .separator(SEPARATOR)
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

    private static List<RollBuilder> reverse(Collection<RollBuilder> collection) {
        List<RollBuilder> result = new ArrayList<>(collection.size());
        for (RollBuilder t : collection) {
            result.addFirst(t);
        }
        return result;
    }

    private static boolean hasStackTokenPrecedence(Token currentToken, Token stackToken) {
        return stackToken.getOperator().isPresent()
                && ((Operator.Associativity.LEFT.equals(currentToken.getOperatorAssociativity().orElseThrow())
                && (currentToken.getOperatorPrecedence().orElseThrow() <= stackToken.getOperatorPrecedence().orElseThrow())) ||
                (currentToken.getOperatorPrecedence().orElseThrow() < stackToken.getOperatorPrecedence().orElseThrow()));
    }

    public static Roller createRollSupplier(List<RollBuilder> rollBuilders) {
        return () -> {
            Map<String, Roll> variableMap = new HashMap<>();
            List<Roll> rolls = RollBuilder.extendAllBuilder(rollBuilders, variableMap);
            if (!variableMap.isEmpty()) {
                //we need to add the val expression in front of the expression
                String variablestring = variableMap.values().stream().map(Roll::getExpression).collect(Collectors.joining(", "));
                rolls = rolls.stream()
                        .map(r -> new Roll("%s, %s".formatted(variablestring, r.getExpression()), r.getElements(), r.getRandomElementsInRoll(), r.getChildrenRolls()))
                        .collect(Collectors.toList());
            }
            return rolls;
        };
    }

    private @NonNull RollBuilder toValue(@NonNull String literal, @NonNull String inputValue) {
        Matcher listMatcher = LIST_REGEX.matcher(literal);
        if (listMatcher.find()) {
            List<String> list = Arrays.asList(listMatcher.group(1).split("[%s%s]".formatted(SEPARATOR, LEGACY_LIST_SEPARATOR)));
            return new RollBuilder() {
                @Override
                public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variableMap) {
                    return Optional.of(ImmutableList.of(new Roll(toExpression(), list.stream()
                            .map(String::trim)
                            .map(s -> new RollElement(s, RollElement.NO_TAG, RollElement.NO_COLOR))
                            .collect(ImmutableList.toImmutableList()), UniqueRandomElements.empty(), ImmutableList.of())));
                }

                @Override
                public @NonNull String toExpression() {
                    return inputValue;
                }
            };
        }
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variables) {
                if (variables.containsKey(literal)) {
                    Roll variableValue = variables.get(literal);
                    //set the input as expression
                    Roll replacedValue = new Roll(toExpression(), variableValue.getElements(), variableValue.getRandomElementsInRoll(), variableValue.getChildrenRolls());
                    return Optional.of(ImmutableList.of(replacedValue));
                }
                if (literal.isEmpty()) {
                    return Optional.of(ImmutableList.of(new Roll(toExpression(), ImmutableList.of(), UniqueRandomElements.empty(), ImmutableList.of())));
                }
                return Optional.of(ImmutableList.of(new Roll(toExpression(), ImmutableList.of(new RollElement(literal, RollElement.NO_TAG, RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of())));
            }

            @Override
            public @NonNull String toExpression() {
                return inputValue;
            }
        };
    }

    private void processTokenToValues(Deque<RollBuilder> values, Token token) throws ExpressionException {
        if (token.getLiteral().isPresent()) { // If the token is a literal, a constant, or a variable name
            String literal = token.getLiteral().get();
            values.push(toValue(literal, token.getInputValue()));

        } else if (token.getOperator().isPresent()) { // If the token is an operator
            Operator operator = token.getOperator().get();
            int argumentCount = token.getOperatorType().orElseThrow().argumentCount;
            if (values.size() < argumentCount) {
                throw new ExpressionException("Not enough values, %s needs %d".formatted(operator.getName(), argumentCount));
            }
            values.push(operator.evaluate(getArguments(values, argumentCount), token.getInputValue()));
        } else {
            throw new ExpressionException(token.toString());
        }
    }

    private void doFunction(Deque<RollBuilder> values, Function function, int argumentCount, String inputValue) throws ExpressionException {
        final RollBuilder res = function.evaluate(getArguments(values, argumentCount), inputValue);
        values.push(res);
    }

    private List<RollBuilder> getArguments(Deque<RollBuilder> values, int argumentCount) {
        // The arguments are in reverse order on the values stack
        List<RollBuilder> result = new ArrayList<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            result.addFirst(values.pop());
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
     * Checks if the provided contains any characters matching an operator or function.
     * Even if the method returns true, the expression can still be invalid.
     */
    public boolean expressionContainsOperatorOrFunction(String expression) {
        return tokenizer.expressionContainsOperatorOrFunction(expression);
    }

    /**
     * Evaluates an expression.
     *
     * @param expression The expression to evaluate
     * @return The result of the evaluation. This can be multiple values, if the expression can't be reduced to a single value.
     * @throws ExpressionException if the expression is not correct.
     */
    public List<Roll> evaluate(String expression) throws ExpressionException {
        return buildRollSupplier(expression).roll();
    }

    /**
     * Create a roller for an expression. The roller is the expression as function can be used again to roll the expression
     * again. Each execution of the roller will generate new random elements.
     *
     * @param expression The expression to evaluate
     * @return A roller that executes the expression
     * @throws ExpressionException if the expression is not correct.
     */
    public Roller buildRollSupplier(String expression) throws ExpressionException {
        expression = expression.trim();
        if (Strings.isNullOrEmpty(expression)) {
            return ImmutableList::of;
        }

        final List<Token> tokens = tokenizer.tokenize(expression);
        final Deque<RollBuilder> values = new ArrayDeque<>(tokens.size()); // values stack
        final LinkedList<Token> stack = new LinkedList<>(); // operators, function and brackets stack
        final Deque<Integer> previousValuesSize = new ArrayDeque<>(tokens.size());
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
                } else if (previous.get().isOpenBracket()) {
                    throw new ExpressionException("empty brackets are not allowed");
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
                    final Token functionToken = stack.pop();
                    doFunction(values, functionToken.getFunction().orElseThrow(), argumentCount, functionToken.getInputValue());
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
                        if (stack.size() < 2 || stack.get(1).getFunction().isEmpty()) {
                            throw new ExpressionException("Separator '%s' in bracket '%s' without leading function is not allowed"
                                    .formatted(parameters.getSeparator(), parameters.getExpressionBrackets().stream()
                                            .map(Object::toString)
                                            .collect(Collectors.joining(","))));
                        }
                    } else {
                        // Until the token at the top of the stack is a left parenthesis,
                        // pop operators off the stack onto the output queue.
                        Token stackToken = stack.pop();
                        processTokenToValues(values, stackToken);
                    }
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
            } else if (token.getLiteral().isPresent()) {
                //if there is a literal following a literal then this is an independent part of the expression
                //and the left part needs to be processed
                //TODO only keep this for backward compatibility, new expression parts should always explicit started with a ','
                if (previous.flatMap(Token::getLiteral).isPresent() && !stack.isEmpty()) {
                    if (!stack.peek().isOpenBracket()) { //no unfinished bracket
                        processTokenToValues(values, stack.pop());
                    } else {
                        throw new ExpressionException("All brackets need to be closed be for starting a new expression or missing ','");
                    }
                }
                // If the token is literal then add its value to the output queue.
                processTokenToValues(values, token);
            } else {
                throw new IllegalStateException("Unknown Token: %s".formatted(token));
            }
            previous = Optional.of(token);
        }
        // When there are no more tokens to read:
        // While there are still operator tokens in the stack:
        for (Token stackToken : stack) {
            if (stackToken.isOpenBracket() || stackToken.isCloseBracket()) {
                throw new ExpressionException("Parentheses mismatched");
            }
            processTokenToValues(values, stackToken);
        }
        return createRollSupplier(reverse(values));
    }
}

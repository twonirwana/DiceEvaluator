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
    private static final int DEFAULT_MAX_NUMBER_OF_ELEMENTS = 10_000;
    private static final boolean DEFAULT_KEEP_CHILDREN_ROLLS = true;

    private static final String SEPARATOR = ",";
    private static final String LEGACY_LIST_SEPARATOR = "/";
    private static final Pattern LIST_REGEX = Pattern.compile("(.+([%s%s].+)+)".formatted(SEPARATOR, LEGACY_LIST_SEPARATOR), Pattern.DOTALL); //the brackets are used for the escape and are not part of the literal
    private final Tokenizer tokenizer;
    private final Parameters parameters;
    private final int maxNumberOfElements;
    private final boolean keepChildrenRolls;
    private final NumberSupplier defaultNumberSupplier;

    public DiceEvaluator() {
        this(new RandomNumberSupplier(), DEFAULT_MAX_NUMBER_OF_DICE, DEFAULT_MAX_NUMBER_OF_ELEMENTS, DEFAULT_KEEP_CHILDREN_ROLLS);
    }

    public DiceEvaluator(@NonNull NumberSupplier numberSupplier, int maxNumberOfDice, int maxNumberOfElements, boolean keepChildrenRolls) {
        this.maxNumberOfElements = maxNumberOfElements;
        this.keepChildrenRolls = keepChildrenRolls;
        this.defaultNumberSupplier = numberSupplier;
        parameters = Parameters.builder()
                .expressionBracket(BracketPair.PARENTHESES)
                .functionBracket(BracketPair.PARENTHESES)
                .escapeBracket(BracketPair.APOSTROPHE)
                .escapeBracket(BracketPair.BRACKETS)
                .operators(ImmutableList.<Operator>builder()
                        .add(new RegularDice(maxNumberOfDice, maxNumberOfElements, keepChildrenRolls))
                        .add(new ExplodingDice(maxNumberOfDice, maxNumberOfElements, keepChildrenRolls))
                        .add(new ExplodingAddDice(maxNumberOfDice, maxNumberOfElements, keepChildrenRolls))
                        .add(new AddToList(maxNumberOfElements, keepChildrenRolls))
                        .add(new Concat(maxNumberOfElements, keepChildrenRolls))
                        .add(new Color(maxNumberOfElements, keepChildrenRolls))
                        .add(new Tag(maxNumberOfElements, keepChildrenRolls))
                        .add(new Sum(maxNumberOfElements, keepChildrenRolls))
                        .add(new Repeat(maxNumberOfElements, keepChildrenRolls))
                        .add(new RepeatList(maxNumberOfElements, keepChildrenRolls))
                        .add(new NegateAddRemove(maxNumberOfElements, keepChildrenRolls))
                        .add(new IntegerDivide(maxNumberOfElements, keepChildrenRolls))
                        .add(new DecimalDivide(maxNumberOfElements, keepChildrenRolls))
                        .add(new Multiply(maxNumberOfElements, keepChildrenRolls))
                        .add(new Modulo(maxNumberOfElements, keepChildrenRolls))
                        .add(new KeepHighest(maxNumberOfElements, keepChildrenRolls))
                        .add(new KeepLowest(maxNumberOfElements, keepChildrenRolls))
                        .add(new GreaterThanFilter(maxNumberOfElements, keepChildrenRolls))
                        .add(new LesserThanFilter(maxNumberOfElements, keepChildrenRolls))
                        .add(new GreaterEqualThanFilter(maxNumberOfElements, keepChildrenRolls))
                        .add(new LesserEqualThanFilter(maxNumberOfElements, keepChildrenRolls))
                        .add(new EqualFilter(maxNumberOfElements, keepChildrenRolls))
                        .add(new Count(maxNumberOfElements, keepChildrenRolls))
                        .add(new Reroll(maxNumberOfElements, keepChildrenRolls))
                        .add(new EqualBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new GreaterBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new GreaterEqualBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new LesserBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new LesserEqualBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new InBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new AndBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new OrBool(maxNumberOfElements, keepChildrenRolls))
                        .add(new NegateBool(maxNumberOfElements, keepChildrenRolls))
                        .build())
                .functions(ImmutableList.<Function>builder()
                        .add(new ColorFunction(maxNumberOfElements, keepChildrenRolls))
                        .add(new Value(maxNumberOfElements, keepChildrenRolls))
                        .add(new ConcatFunction(maxNumberOfElements, keepChildrenRolls))
                        .add(new SortAsc(maxNumberOfElements, keepChildrenRolls))
                        .add(new SortDesc(maxNumberOfElements, keepChildrenRolls))
                        .add(new Min(maxNumberOfElements, keepChildrenRolls))
                        .add(new Max(maxNumberOfElements, keepChildrenRolls))
                        .add(new Cancel(maxNumberOfElements, keepChildrenRolls))
                        .add(new Double(maxNumberOfElements, keepChildrenRolls))
                        .add(new IfEqual(maxNumberOfElements, keepChildrenRolls))
                        .add(new If(maxNumberOfElements, keepChildrenRolls))
                        .add(new IfGreater(maxNumberOfElements, keepChildrenRolls))
                        .add(new IfIn(maxNumberOfElements, keepChildrenRolls))
                        .add(new Replace(maxNumberOfElements, keepChildrenRolls))
                        .add(new ColorOn(maxNumberOfElements, keepChildrenRolls))
                        .add(new Explode(maxNumberOfElements, keepChildrenRolls))
                        .add(new IfLesser(maxNumberOfElements, keepChildrenRolls))
                        .add(new GroupCount(maxNumberOfElements, keepChildrenRolls))
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

    private Roller createRollSupplier(String expression, List<RollBuilder> rollBuilders) {
        return new Roller() {
            @Override
            public @NonNull RollResult roll() throws ExpressionException {
                return rollWithNumberSupplier(expression, defaultNumberSupplier, rollBuilders);
            }

            @Override
            public @NonNull RollResult roll(NumberSupplier numberSupplier) throws ExpressionException {
                return rollWithNumberSupplier(expression, numberSupplier, rollBuilders);
            }
        };
    }

    private RollResult rollWithNumberSupplier(String expression, NumberSupplier numberSupplier, List<RollBuilder> rollBuilders) throws ExpressionException {
        RollContext rollContext = new RollContext(numberSupplier);
        ImmutableList<Roll> rolls = RollBuilder.extendAllBuilder(rollBuilders, rollContext);
        Optional<String> expressionPrefix = rollContext.getExpressionPrefixString();
        if (expressionPrefix.isPresent()) {
            //we need to add the val expression in front of the expression
            ImmutableList.Builder<Roll> rollBuilder = ImmutableList.builder();
            for (Roll r : rolls) {
                String newExpressionString = "%s, %s".formatted(expressionPrefix.get(), r.getExpression());
                rollBuilder.add(new Roll(newExpressionString,
                        r.getElements(),
                        r.getRandomElementsInRoll(),
                        r.getChildrenRolls(),
                        r.getExpressionPosition(),
                        maxNumberOfElements,
                        keepChildrenRolls));
            }
            rolls = rollBuilder.build();
        }
        return new RollResult(expression, rolls, rollContext.getAllRandomElements());
    }

    private @NonNull RollBuilder toValue(@NonNull String literal, @NonNull ExpressionPosition expressionPosition) {
        Matcher listMatcher = LIST_REGEX.matcher(literal);
        if (listMatcher.find()) {
            List<String> list = Arrays.asList(listMatcher.group(1).split("[%s%s]".formatted(SEPARATOR, LEGACY_LIST_SEPARATOR)));
            return new RollBuilder() {
                @Override
                public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                    return Optional.of(ImmutableList.of(new Roll(toExpression(), list.stream()
                            .map(String::trim)
                            .map(s -> new RollElement(s, RollElement.NO_TAG, RollElement.NO_COLOR))
                            .collect(ImmutableList.toImmutableList()), ImmutableList.of(), ImmutableList.of(), expressionPosition,
                            maxNumberOfElements, keepChildrenRolls)));
                }

                @Override
                public @NonNull String toExpression() {
                    return expressionPosition.toStringWithExtension();
                }
            };
        }
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                Optional<Roll> variableRoll = rollContext.getVariable(literal);
                if (variableRoll.isPresent()) {
                    Roll variableValue = variableRoll.get();
                    //set the input as expression
                    Roll replacedValue = new Roll(toExpression(), variableValue.getElements(), variableValue.getRandomElementsInRoll(), variableValue.getChildrenRolls(), expressionPosition, maxNumberOfElements, keepChildrenRolls);
                    return Optional.of(ImmutableList.of(replacedValue));
                }
                if (literal.isEmpty()) {
                    return Optional.of(ImmutableList.of(new Roll(toExpression(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), expressionPosition, maxNumberOfElements, keepChildrenRolls)));
                }
                return Optional.of(ImmutableList.of(new Roll(toExpression(), ImmutableList.of(new RollElement(literal, RollElement.NO_TAG, RollElement.NO_COLOR)), ImmutableList.of(), ImmutableList.of(), expressionPosition, maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return expressionPosition.toStringWithExtension();
            }
        };
    }

    private void processTokenToValues(Deque<RollBuilder> values, Token token) throws ExpressionException {
        if (token.getLiteral().isPresent()) { // If the token is a literal, a constant, or a variable name
            String literal = token.getLiteral().get();
            values.push(toValue(literal, token.getExpressionPosition()));

        } else if (token.getOperator().isPresent()) { // If the token is an operator
            Operator operator = token.getOperator().get();
            int argumentCount = token.getOperatorType().orElseThrow().argumentCount;
            if (values.size() < argumentCount) {
                throw new ExpressionException("Not enough values, %s needs %d".formatted(operator.getName(), argumentCount), token.getExpressionPosition());
            }
            values.push(operator.evaluate(getArguments(values, argumentCount), token.getExpressionPosition()));
        } else {
            throw new ExpressionException(token.toString(), token.getExpressionPosition());
        }
    }

    private void doFunction(Deque<RollBuilder> values, Function function, int argumentCount, ExpressionPosition expressionPosition) throws ExpressionException {
        final RollBuilder res = function.evaluate(getArguments(values, argumentCount), expressionPosition);
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
     * @return The result of the evaluation.
     * @throws ExpressionException if the expression is not correct.
     */
    public RollResult evaluate(String expression) throws ExpressionException {
        return buildRollSupplier(expression).roll();
    }

    /**
     * Create a roller for an expression. The roller is the expression as function can be used again to roll the expression
     * again. Each execution of the roller will generate new random elements.
     *
     * @param inputExpression The expression to evaluate
     * @return A roller that executes the expression
     * @throws ExpressionException if the expression is not correct.
     */
    public Roller buildRollSupplier(final String inputExpression) throws ExpressionException {
        final String expression = inputExpression.trim();
        if (Strings.isNullOrEmpty(expression)) {
            return new Roller() {
                @Override
                public @NonNull RollResult roll() {
                    return new RollResult(expression, ImmutableList.of(), ImmutableList.of());
                }

                @Override
                public @NonNull RollResult roll(NumberSupplier numberSupplier) {
                    return new RollResult(expression, ImmutableList.of(), ImmutableList.of());
                }
            };
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
                throw new ExpressionException("A function, in this case '%s', must be followed a open function bracket: %s".formatted(functionName, allowedBrackets), token.getExpressionPosition());
            }

            if (token.getBrackets().isPresent() && token.isOpenBracket()) {
                // If the token is a left parenthesis, then push it onto the stack.
                stack.push(token);
                if (previous.flatMap(Token::getFunction).isPresent()) {
                    if (!parameters.getFunctionBrackets().contains(token.getBrackets().get())) {
                        throw new ExpressionException("Invalid bracket after function: %s".formatted(token), token.getExpressionPosition());
                    }
                } else {
                    if (!parameters.getExpressionBrackets().contains(token.getBrackets().get())) {
                        throw new ExpressionException("Invalid bracket in expression: %s".formatted(token), token.getExpressionPosition());
                    }
                }
            } else if (token.isCloseBracket()) {
                if (previous.isEmpty()) {
                    throw new ExpressionException("expression can't start with a close bracket", token.getExpressionPosition());
                } else if (previous.get().isOpenBracket()) {
                    throw new ExpressionException("empty brackets are not allowed", token.getExpressionPosition());
                }
                if (previous.map(Token::isSeparator).get()) {
                    throw new ExpressionException("argument is missing", token.getExpressionPosition());
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
                            throw new ExpressionException("Invalid parenthesis match %s%s".formatted(stackToken.getBrackets().get().getOpen(), brackets.getClose()), token.getExpressionPosition());
                        }
                    } else {
                        processTokenToValues(values, stackToken);
                    }
                }
                if (!openBracketFound) {
                    // If the stack runs out without finding a left parenthesis, then
                    // there are mismatched parentheses.
                    throw new ExpressionException("Parentheses mismatched", token.getExpressionPosition());
                }
                if (!stack.isEmpty() && stack.peek().getFunction().isPresent()) {
                    // If the token at the top of the stack is a function token, pop it
                    // onto the output queue.
                    int argumentCount = values.size() - previousValuesSize.pop();
                    final Token functionToken = stack.pop();
                    doFunction(values, functionToken.getFunction().orElseThrow(), argumentCount, functionToken.getExpressionPosition());
                }
            } else if (token.isSeparator()) {
                if (previous.isEmpty()) {
                    throw new ExpressionException("expression can't start with a separator", token.getExpressionPosition());
                }
                // Verify that there was an argument before this separator
                if (previous.get().isOpenBracket() || previous.get().isSeparator()) {
                    throw new ExpressionException("A separator can't be followed by another separator or open bracket", token.getExpressionPosition());
                }
                boolean openBracketOnStackReached = false;

                while (!stack.isEmpty() && !openBracketOnStackReached) {
                    if (stack.peek().isOpenBracket()) {
                        openBracketOnStackReached = true;
                        if (stack.size() < 2 || stack.get(1).getFunction().isEmpty()) {
                            throw new ExpressionException("Separator '%s' in bracket '%s' without leading function is not allowed"
                                    .formatted(parameters.getSeparator(), parameters.getExpressionBrackets().stream()
                                            .map(Object::toString)
                                            .collect(Collectors.joining(","))), token.getExpressionPosition());
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
                        throw new ExpressionException("All brackets need to be closed be for starting a new expression or missing ','", token.getExpressionPosition());
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
                throw new ExpressionException("Parentheses mismatched", stackToken.getExpressionPosition());
            }
            processTokenToValues(values, stackToken);
        }
        return createRollSupplier(expression, reverse(values));
    }
}

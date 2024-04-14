package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Tokenizer {
    private final static String ALL_NUMBER_REGEX = "\\d+\\.?\\d*";
    private final static Pattern SMALL_DECIMAL_PATTERN = Pattern.compile("\\d{1,9}\\.\\d{1,9}");
    private final static Pattern SMALL_INTEGER_PATTERN = Pattern.compile("\\d{1,9}");
    private final ImmutableList<TokenBuilder> tokenBuilders;
    private final String escapeCharacter;
    private final ImmutableList<Pattern> allOperatorAndFunctionNamePatterns;

    public Tokenizer(Parameters parameters) {
        escapeCharacter = parameters.getEscapeBrackets().stream()
                .map(BracketPair::toString).collect(Collectors.joining(" or "));
        ImmutableList.Builder<TokenBuilder> builder = ImmutableList.builder();
        Stream.concat(parameters.getExpressionBrackets().stream(), parameters.getFunctionBrackets().stream())
                .distinct() //expression and function brackets are allowed to contain the same elements
                .forEach(c -> {
                    builder.add(new TokenBuilder(escapeForRegexAndAddCaseInsensitivity(c.getOpen()), expressionPosition -> Token.openTokenOf(c, expressionPosition), false));
                    builder.add(new TokenBuilder(escapeForRegexAndAddCaseInsensitivity(c.getClose()), expressionPosition -> Token.closeTokenOf(c, expressionPosition), false));
                });
        parameters.getFunctions().forEach(function -> builder.add(new TokenBuilder(escapeForRegexAndAddCaseInsensitivity(function.getName()), expressionPosition -> Token.of(function, expressionPosition), false)));
        parameters.getOperators().forEach(operator -> builder.add(new TokenBuilder(escapeForRegexAndAddCaseInsensitivity(operator.getName()), expressionPosition -> Token.of(operator, expressionPosition), false)));
        builder.add(new TokenBuilder(escapeForRegexAndAddCaseInsensitivity(parameters.getSeparator()), Token::separator, false));
        parameters.getEscapeBrackets().forEach(b -> builder.add(new TokenBuilder(buildEscapeBracketsRegex(b), expressionPosition -> Token.of(expressionPosition.value().substring(1, expressionPosition.value().length() - 1), expressionPosition), true)));
        builder.add(new TokenBuilder(ALL_NUMBER_REGEX, expressionPosition -> {
            if (SMALL_INTEGER_PATTERN.matcher(expressionPosition.value()).matches() || SMALL_DECIMAL_PATTERN.matcher(expressionPosition.value()).matches()) {
                return Token.of(expressionPosition.value(), expressionPosition);
            }
            throw new ExpressionException("The number '%s' is too big".formatted(expressionPosition.value()));
        }, false));
        tokenBuilders = builder.build();

        List<String> duplicateRegex = tokenBuilders.stream().collect(Collectors.groupingBy(TokenBuilder::regex))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicateRegex.isEmpty()) {
            throw new IllegalArgumentException("The following regex for tokenizing where used more then once: " + duplicateRegex);
        }
        allOperatorAndFunctionNamePatterns = Stream.concat(
                        parameters.getOperators().stream().map(Operator::getName),
                        parameters.getFunctions().stream().map(Function::getName))
                .map(n -> Pattern.compile(escapeForRegexAndAddCaseInsensitivity(n)))
                .collect(ImmutableList.toImmutableList());
    }

    private static String buildEscapeBracketsRegex(BracketPair bracketPair) {
        return String.format("%s.*?%s", escapeForRegexAndAddCaseInsensitivity(bracketPair.getOpen()), escapeForRegexAndAddCaseInsensitivity(bracketPair.getClose()));
    }

    private static String escapeForRegexAndAddCaseInsensitivity(String in) {
        return "(?i)\\Q%s\\E(?-i)".formatted(in);
    }

    public List<Token> tokenize(final String input) throws ExpressionException {
        List<Token> preTokens = new ArrayList<>();
        String current = input.trim();
        Optional<Token> currentMatch;
        int currentPosition = 0;
        do {
            currentMatch = getBestMatch(current, currentPosition);
            if (currentMatch.isPresent()) {
                Token token = currentMatch.get();
                preTokens.add(token);
                int matchLength = token.getExpressionPosition().value().length();
                current = current.substring(matchLength).trim();
                currentPosition += matchLength;
            }
        } while (currentMatch.isPresent());
        if (!current.isEmpty()) {
            throw new ExpressionException("No matching operator for '%s', non-functional text and value names must to be surrounded by %s".formatted(current, escapeCharacter));
        }

        return setOperatorType(preTokens);
    }

    private List<Token> setOperatorType(List<Token> in) throws ExpressionException {
        ImmutableList.Builder<Token> builder = ImmutableList.builder();
        boolean lastOperatorWasUnaryLeft = false;
        for (int i = 0; i < in.size(); i++) {
            Token token = in.get(i);
            Optional<String> previousOpenBracket = getPreviousOpenBrackets(in, i);
            if (previousOpenBracket.isPresent()) {
                token = Token.addOpenBracket(token, previousOpenBracket.get());
            }
            Optional<String> followingCloseBracket = getFollowingCloseBrackets(in, i);
            if (followingCloseBracket.isPresent()) {
                token = Token.addCloseBracket(token, followingCloseBracket.get());
            }
            if (token.getOperator().isPresent()) {
                Token left = i == 0 ? null : in.get(i - 1);
                Token right = i == in.size() - 1 ? null : in.get(i + 1);
                Operator.OperatorType type = determineAndValidateOperatorType(token.getOperator().get(), left, right, lastOperatorWasUnaryLeft);
                builder.add(Token.of(token.getOperator().get(), type, token.getExpressionPosition()));
                lastOperatorWasUnaryLeft = type == Operator.OperatorType.UNARY && token.getOperator().get().getAssociativityForOperantType(Operator.OperatorType.UNARY) == Operator.Associativity.LEFT;
            } else {
                builder.add(token);
                lastOperatorWasUnaryLeft = false;
            }

        }
        return builder.build();
    }

    private Optional<String> getPreviousOpenBrackets(List<Token> in, int index) {
        if (index <= 0) {
            return Optional.empty();
        }
        int i = index - 1;
        StringBuilder brackets = new StringBuilder();
        while (i >= 0 && in.get(i).getBrackets().isPresent() && in.get(i).isOpenBracket()) {
            brackets.append(in.get(i).getBrackets().get().getOpen());
            i--;
        }
        if (brackets.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(brackets.toString());
    }

    private Optional<String> getFollowingCloseBrackets(List<Token> in, int index) {
        if (index > in.size() - 2) {
            return Optional.empty();
        }

        int i = index + 1;
        StringBuilder brackets = new StringBuilder();
        while (i < in.size() && in.get(i).getBrackets().isPresent() && in.get(i).isCloseBracket()) {
            brackets.append(in.get(i).getBrackets().get().getClose());
            i++;
        }
        if (brackets.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(brackets.toString());
    }

    private Operator.OperatorType determineAndValidateOperatorType(@NonNull Operator operator, @Nullable Token left, @Nullable Token right, boolean lastOperatorWasUnaryLeft) throws ExpressionException {
        //todo cleanup
        boolean leftLiteralOrBracket = left != null && (left.getLiteral().isPresent() || left.isCloseBracket() || (left.getOperator().isPresent() && lastOperatorWasUnaryLeft));
        boolean rightLiteralOrBracket = right != null && (right.getLiteral().isPresent() || right.isOpenBracket() ||
                (right.getOperator().isPresent() && right.getOperator().get().getAssociativityForOperantType(Operator.OperatorType.UNARY) == Operator.Associativity.RIGHT)
                || (right.getFunction().isPresent()));

        if (leftLiteralOrBracket && rightLiteralOrBracket) {
            if (!operator.supportBinaryOperation()) {
                throw new ExpressionException("Operator %s does not support binary operations".formatted(operator.getName()));
            }
            return Operator.OperatorType.BINARY;
        }
        if (!operator.supportUnaryOperation()) {
            throw new ExpressionException("Operator %s does not support unary operations".formatted(operator.getName()));
        }
        Operator.Associativity operatorAssociativity = operator.getAssociativityForOperantType(Operator.OperatorType.UNARY);
        if (operatorAssociativity == Operator.Associativity.LEFT && !leftLiteralOrBracket) {
            throw new ExpressionException("Operator %s has left associativity but the left value was: %s".formatted(operator.getName(), Optional.ofNullable(left).map(Object::toString).orElse("empty")));
        }
        if (operatorAssociativity == Operator.Associativity.RIGHT && !rightLiteralOrBracket) {
            throw new ExpressionException("Operator %s has right associativity but the right value was: %s".formatted(operator.getName(), Optional.ofNullable(right).map(Object::toString).orElse("empty")));
        }

        return Operator.OperatorType.UNARY;
    }

    private Optional<Token> getBestMatch(String input, int position) throws ExpressionException {
        List<Token> allMatches = getAllMatches(input, position);
        int maxLength = allMatches.stream()
                .map(Token::getExpressionPosition)
                .map(ExpressionPosition::value)
                .mapToInt(String::length)
                .max()
                .orElse(0);
        List<Token> maxLengthMatches = allMatches.stream()
                .filter(m -> m.getExpressionPosition().value().length() == maxLength)
                .toList();
        if (maxLengthMatches.isEmpty()) {
            return Optional.empty();
        }
        if (maxLengthMatches.size() > 1) {
            throw new IllegalStateException("More then one operator matched the input %s: %s".formatted(input, maxLengthMatches.stream().map(Token::toString).toList()));
        }

        return Optional.of(maxLengthMatches.getFirst());
    }

    private List<Token> getAllMatches(String input, int position) throws ExpressionException {
        ImmutableList.Builder<Token> matchBuilder = ImmutableList.builder();
        for (Tokenizer.TokenBuilder tokenBuilder : tokenBuilders) {
            Optional<Token> firstMatch = getFirstMatch(input, tokenBuilder, position);
            firstMatch.ifPresent(matchBuilder::add);
        }
        return matchBuilder.build();
    }

    private Optional<Token> getFirstMatch(String input, TokenBuilder tokenBuilder, int position) throws ExpressionException {
        Matcher matcher = tokenBuilder.pattern().matcher(input);
        if (matcher.find()) {
            String matchGroup = matcher.group().trim();
            return Optional.of(tokenBuilder.toToken().apply(ExpressionPosition.of(position, matchGroup)));
        }
        return Optional.empty();
    }

    public boolean expressionContainsOperatorOrFunction(String expression) {
        return allOperatorAndFunctionNamePatterns.stream().anyMatch(p -> p.matcher(expression).find());
    }

    private interface ToToken {
        Token apply(ExpressionPosition expressionPosition) throws ExpressionException;
    }

    private record TokenBuilder(String regex, ToToken toToken, boolean multiLine) {
        Pattern pattern() {
            if (multiLine) {
                return Pattern.compile("^\\s*%s\\s*".formatted(regex), Pattern.DOTALL);
            }
            return Pattern.compile("^\\s*%s\\s*".formatted(regex));
        }
    }
}

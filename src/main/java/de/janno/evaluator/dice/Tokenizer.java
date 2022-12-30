package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Tokenizer {
    private final ImmutableList<TokenBuilder> tokenBuilders;
    private final String escapeCharacter;

    public Tokenizer(Parameters parameters) {
        escapeCharacter = parameters.getEscapeBrackets().stream()
                .map(BracketPair::toString).collect(Collectors.joining(" or "));
        ImmutableList.Builder<TokenBuilder> builder = ImmutableList.builder();
        Stream.concat(parameters.getExpressionBrackets().stream(), parameters.getFunctionBrackets().stream())
                .distinct() //expression and function brackets are allowed to contain the same elements
                .forEach(c -> {
                    builder.add(new TokenBuilder(escapeForRegex(c.getOpen()), s -> Token.openTokenOf(c)));
                    builder.add(new TokenBuilder(escapeForRegex(c.getClose()), s -> Token.closeTokenOf(c)));
                });
        parameters.getFunctions().forEach(function -> function.getNames().forEach(name -> builder.add(new TokenBuilder(escapeForRegex(name), s -> Token.of(function)))));
        parameters.getOperators().forEach(operator -> operator.getNames().forEach(name -> builder.add(new TokenBuilder(escapeForRegex(name), s -> Token.of(operator)))));
        builder.add(new TokenBuilder(escapeForRegex(parameters.getSeparator()), s -> Token.separator()));
        parameters.getEscapeBrackets().forEach(b -> builder.add(new TokenBuilder(buildEscapeBracketsRegex(b), s -> Token.of(s.substring(1, s.length() - 1)))));
        builder.add(new TokenBuilder("[0-9]+", Token::of));
        tokenBuilders = builder.build();

        List<String> duplicateRegex = tokenBuilders.stream().collect(Collectors.groupingBy(TokenBuilder::regex))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicateRegex.isEmpty()) {
            throw new IllegalArgumentException("The following regex for tokenizing where used more then once: " + duplicateRegex);
        }
    }

    private static String buildEscapeBracketsRegex(BracketPair bracketPair) {
        return String.format("%s.*?%s", escapeForRegex(bracketPair.getOpen()), escapeForRegex(bracketPair.getClose()));
    }

    private static String escapeForRegex(String in) {
        return "\\Q" + in + "\\E";
    }

    public List<Token> tokenize(final String input) throws ExpressionException {
        List<Token> preTokens = new ArrayList<>();
        String current = input.trim();
        Optional<Match> currentMatch;
        do {
            currentMatch = getBestMatch(current);
            if (currentMatch.isPresent()) {
                Match match = currentMatch.get();
                if (match.start() != 0) {
                    throw new ExpressionException("No matching operator at the start of '%s', non-functional text need to be surrounded by %s".formatted(current, escapeCharacter));
                }
                Token token = match.token();
                preTokens.add(token);
                current = current.substring(match.match().length()).trim();
            }
        } while (currentMatch.isPresent());
        if (!current.isEmpty()) {
            throw new ExpressionException("No matching operator for '%s', non-functional text need to be surrounded by %s".formatted(current, escapeCharacter));
        }

        return setOperatorType(preTokens);
    }

    private List<Token> setOperatorType(List<Token> in) throws ExpressionException {
        ImmutableList.Builder<Token> builder = ImmutableList.builder();
        boolean lastOperatorWasUnaryLeft = false;
        for (int i = 0; i < in.size(); i++) {
            Token token = in.get(i);
            if (token.getOperator().isPresent()) {
                Token left = i == 0 ? null : in.get(i - 1);
                Token right = i == in.size() - 1 ? null : in.get(i + 1);
                Operator.OperatorType type = determineAndValidateOperatorType(token.getOperator().get(), left, right, lastOperatorWasUnaryLeft);
                builder.add(Token.of(token.getOperator().get(), type));
                lastOperatorWasUnaryLeft = type == Operator.OperatorType.UNARY && token.getOperator().get().getAssociativityForOperantType(Operator.OperatorType.UNARY) == Operator.Associativity.LEFT;
            } else {
                builder.add(token);
                lastOperatorWasUnaryLeft = false;
            }

        }
        return builder.build();
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

    private Optional<Match> getBestMatch(String input) {
        List<Match> allMatches = getAllMatches(input);

        int minStart = allMatches.stream()
                .mapToInt(Match::start)
                .min().orElse(-1);
        List<Match> minStartMatches = allMatches.stream()
                .filter(m -> m.start() == minStart)
                .toList();
        int maxLength = minStartMatches.stream()
                .mapToInt(Match::length)
                .max()
                .orElse(0);
        List<Match> minStartMaxLengthMatches = minStartMatches.stream()
                .filter(m -> m.length() == maxLength)
                .toList();
        if (minStartMaxLengthMatches.isEmpty()) {
            return Optional.empty();
        }
        if (minStartMaxLengthMatches.size() > 1) {
            throw new IllegalStateException("More then one operator matched the input %s: %s".formatted(input, minStartMaxLengthMatches.stream().map(Match::token).map(Token::toString).toList()));
        }

        return Optional.of(minStartMaxLengthMatches.get(0));
    }


    private List<Match> getAllMatches(String input) {
        return tokenBuilders.stream()
                .map(p -> getFirstMatch(input, p))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Match> getFirstMatch(String input, TokenBuilder tokenBuilder) {
        Matcher matcher = tokenBuilder.pattern().matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0 || matcher.end() != 0) {
                String matchGroup = matcher.group().trim();
                return Optional.of(new Match(matcher.start(), matchGroup, tokenBuilder.toToken().apply(matchGroup)));
            }
        }
        return Optional.empty();
    }

    private record Match(int start, String match, Token token) {
        public int length() {
            return match.length();
        }
    }


    private record TokenBuilder(String regex, Function<String, Token> toToken) {
        Pattern pattern() {
            return Pattern.compile("^\\s*" + regex + "\\s*");
        }
    }
}

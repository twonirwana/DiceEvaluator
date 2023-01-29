package de.janno.evaluator.dice;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Optional;

@EqualsAndHashCode
public class Token {
    @NonNull
    private final Kind kind;
    @NonNull
    private final String inputValue;
    private final Operator operator;
    private final Function function;
    private final String literal;
    private final BracketPair bracketPair;
    // The operator type, determine by the left and right token
    private final Operator.OperatorType operatorType;


    private Token(@NonNull Kind kind, @NonNull String inputValue, Operator operator, Function function, String literal, Operator.OperatorType operatorType, BracketPair bracketPair) {
        this.kind = kind;
        this.inputValue = inputValue;
        this.operator = operator;
        this.function = function;
        this.literal = literal;
        this.operatorType = operatorType;
        this.bracketPair = bracketPair;
    }

    public static Token addOpenBracket(Token token, String brackets) {
        return new Token(token.kind, brackets + token.getInputValue(), token.operator, token.function, token.literal, token.operatorType, token.bracketPair);
    }

    public static Token addCloseBracket(Token token, String brackets) {
        return new Token(token.kind, token.getInputValue() + brackets, token.operator, token.function, token.literal, token.operatorType, token.bracketPair);
    }

    public static Token of(Operator operator, String inputValue) {
        return new Token(Kind.OPERATOR, inputValue, operator, null, null, null, null);
    }

    public static Token of(Operator operator, Operator.OperatorType operatorType, String inputValue) {
        if (operatorType == Operator.OperatorType.UNARY && !operator.supportUnaryOperation()) {
            throw new IllegalArgumentException(operator + "only supports binary operation");
        }
        if (operatorType == Operator.OperatorType.BINARY && !operator.supportBinaryOperation()) {
            throw new IllegalArgumentException(operator + "only supports unary operation");
        }
        return new Token(Kind.OPERATOR, inputValue, operator, null, null, operatorType, null);
    }

    public static Token of(Function function, String inputValue) {
        return new Token(Kind.FUNCTION, inputValue, null, function, null, null, null);
    }

    public static Token of(String literal, String inputValue) {
        return new Token(Kind.LITERAL, inputValue, null, null, literal, null, null);
    }

    public static Token openTokenOf(BracketPair bracketPair, String inputValue) {
        return new Token(Kind.OPEN_BRACKET, inputValue, null, null, null, null, bracketPair);
    }

    public static Token closeTokenOf(BracketPair bracketPair, String inputValue) {
        return new Token(Kind.CLOSE_BRACKET, inputValue, null, null, null, null, bracketPair);
    }

    public static Token separator(String inputValue) {
        return new Token(Kind.SEPARATOR, inputValue, null, null, null, null, null);
    }

    public Optional<BracketPair> getBrackets() {
        return Optional.ofNullable(bracketPair);
    }

    public Optional<Operator> getOperator() {
        return Optional.ofNullable(operator);
    }

    public Optional<Operator.OperatorType> getOperatorType() {
        return Optional.ofNullable(operatorType);
    }

    public Optional<Function> getFunction() {
        return Optional.ofNullable(function);
    }

    public Optional<String> getLiteral() {
        return Optional.ofNullable(literal);
    }

    public boolean isOpenBracket() {
        return kind.equals(Kind.OPEN_BRACKET);
    }

    public boolean isCloseBracket() {
        return kind.equals(Kind.CLOSE_BRACKET);
    }

    public boolean isSeparator() {
        return kind.equals(Kind.SEPARATOR);
    }

    public Optional<Operator.Associativity> getOperatorAssociativity() {
        return getOperator().map(o -> o.getAssociativityForOperantType(operatorType));
    }

    public Optional<Integer> getOperatorPrecedence() {
        return getOperator().map(o -> o.getPrecedenceForOperantType(operatorType));
    }

    public @NonNull String getInputValue() {
        return inputValue;
    }

    @Override
    public String toString() {
        return switch (kind) {
            case OPERATOR -> operator.getName();
            case FUNCTION -> function.getName();
            case LITERAL -> "'%s'".formatted(literal);
            case OPEN_BRACKET -> bracketPair.getOpen();
            case CLOSE_BRACKET -> bracketPair.getClose();
            case SEPARATOR -> "SEPARATOR";
        };
    }

    private enum Kind {
        OPEN_BRACKET,
        CLOSE_BRACKET,
        SEPARATOR,
        FUNCTION,
        OPERATOR,
        LITERAL
    }

}

package de.janno.evaluator.dice;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Optional;

@EqualsAndHashCode
public class Token {
    @NonNull
    private final Kind kind;
    private final Operator operator;
    private final Function function;
    private final String literal;
    private final BracketPair bracketPair;
    // The operator type, determine by the left and right token
    private final Operator.OperatorType operatorType;

    private Token(@NonNull Kind kind, Operator operator, Function function, String literal, Operator.OperatorType operatorType, BracketPair bracketPair) {
        this.kind = kind;
        this.operator = operator;
        this.function = function;
        this.literal = literal;
        this.operatorType = operatorType;
        this.bracketPair = bracketPair;
    }

    public static Token of(Operator operator) {
        return new Token(Kind.OPERATOR, operator, null, null, null, null);
    }

    public static Token of(Operator operator, Operator.OperatorType operatorType) {
        if (operatorType == Operator.OperatorType.UNARY && !operator.supportUnaryOperation()) {
            throw new IllegalArgumentException(operator + "only supports binary operation");
        }
        if (operatorType == Operator.OperatorType.BINARY && !operator.supportBinaryOperation()) {
            throw new IllegalArgumentException(operator + "only supports unary operation");
        }
        return new Token(Kind.OPERATOR, operator, null, null, operatorType, null);
    }

    public static Token of(Function function) {
        return new Token(Kind.FUNCTION, null, function, null, null, null);
    }

    public static Token of(String literal) {
        return new Token(Kind.LITERAL, null, null, literal, null, null);
    }

    public static Token openTokenOf(BracketPair bracketPair) {
        return new Token(Kind.OPEN_BRACKET, null, null, null, null, bracketPair);
    }

    public static Token closeTokenOf(BracketPair bracketPair) {
        return new Token(Kind.CLOSE_BRACKET, null, null, null, null, bracketPair);
    }

    public static Token separator() {
        return new Token(Kind.SEPARATOR, null, null, null, null, null);
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

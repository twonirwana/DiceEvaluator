package de.janno.evaluator.dice;

import lombok.NonNull;

public record ExpressionPosition(
        int startInc,
        @NonNull String value
) {
    public static ExpressionPosition of(final int startInc, final String value) {
        return new ExpressionPosition(startInc, value);
    }

    public ExpressionPosition extendLeft(final String leftValue) {
        return ExpressionPosition.of(
                startInc - leftValue.length(),
                leftValue + value);
    }

    public ExpressionPosition extendRight(final String rightValue) {
        return ExpressionPosition.of(
                startInc,
                value + rightValue);
    }

    @Override
    public String toString() {
        return startInc + value;
    }
}

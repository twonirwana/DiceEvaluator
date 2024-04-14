package de.janno.evaluator.dice;

import lombok.NonNull;

public record ExpressionPosition(
        int startInc,
        int endInc,
        @NonNull String value
) {
    public static ExpressionPosition of(final int startInc, final int endInc, final String value) {
        return new ExpressionPosition(startInc, endInc, value);
    }

    public ExpressionPosition extendLeft(final String leftValue) {
        return ExpressionPosition.of(
                startInc - leftValue.length(),
                endInc,
                leftValue + value);
    }

    public ExpressionPosition extendRight(final String rightValue) {
        return ExpressionPosition.of(
                startInc,
                endInc + rightValue.length(),
                value + rightValue);
    }
}

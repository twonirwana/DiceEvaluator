package de.janno.evaluator.dice;

import lombok.NonNull;
import lombok.Value;


@Value(staticConstructor = "of")
public class ExpressionPosition implements Comparable<ExpressionPosition> {
    int startInc;
    @NonNull
    String value;

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

    @Override
    public int compareTo(ExpressionPosition o) {
        return Integer.compare(startInc, o.startInc);
    }
}

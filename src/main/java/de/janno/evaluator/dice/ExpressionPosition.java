package de.janno.evaluator.dice;

import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.Value;

import org.jetbrains.annotations.Nullable;


@Value
public class ExpressionPosition implements Comparable<ExpressionPosition> {
    int startInc;
    @NonNull
    String value;
    /**
     * A non operator/function extension on the left, like a parentheses. Needed to build the expression back together.
     */
    @Nullable
    String leftExtension;
    /**
     * A non operator/function extension on the right, like a parentheses. Needed to build the expression back together.
     */
    @Nullable
    String rightExtension;

    public static ExpressionPosition of(final int startInc, final String value) {
        return new ExpressionPosition(startInc, value, null, null);
    }

    public ExpressionPosition extendLeft(final String leftValue) {
        return new ExpressionPosition(this.startInc, this.value, leftValue, this.rightExtension);
    }

    public ExpressionPosition extendRight(final String rightValue) {
        return new ExpressionPosition(this.startInc, this.value, this.leftExtension, rightValue);
    }

    @Override
    public String toString() {
        return startInc + value;
    }

    public String toStringWithExtension() {
        return Joiner.on("").skipNulls().join(leftExtension, value, rightExtension);
    }

    @Override
    public int compareTo(ExpressionPosition o) {
        return Integer.compare(startInc, o.startInc);
    }
}

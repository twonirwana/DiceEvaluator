package de.janno.evaluator.dice;

import lombok.NonNull;
import lombok.Value;


@Value(staticConstructor = "of")
public class RollId implements Comparable<RollId> {
    @NonNull
    ExpressionPosition expressionPosition;
    int reevaluate;

    @Override
    public String toString() {
        return expressionPosition + "e" + reevaluate;
    }

    @Override
    public int compareTo(RollId o) {
        if (!expressionPosition.equals(o.expressionPosition)) {
            return expressionPosition.compareTo(o.expressionPosition);
        }
        return Integer.compare(reevaluate, o.reevaluate);
    }
}

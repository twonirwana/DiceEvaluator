package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

@Value
public class RollResult {
    /**
     * The expression that was the input for the roll.
     */
    @NonNull
    String expression;
    /**
     * The result of the expression roll. This can be multiple values, if the expression can't be reduced to a single value.
     */
    @NonNull
    ImmutableList<Roll> rolls;

    /**
     * all random elements in the total result, this can be more as all the sum of randomElements in the rolls because
     * some function produce empty Rolls, that are removed
     */
    @NonNull
    ImmutableList<RandomElement> allRandomElements;
}

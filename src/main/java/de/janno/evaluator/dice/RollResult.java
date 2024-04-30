package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import javax.annotation.concurrent.Immutable;
import java.util.List;

@Value
public class RollResult {
    /**
     * The expression that was the input for the roll.
     */
    String expression;
    /**
     * The result of the expression roll. This can be multiple values, if the expression can't be reduced to a single value.
     */
    ImmutableList<Roll> rolls;
}

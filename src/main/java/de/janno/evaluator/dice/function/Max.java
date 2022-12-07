package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;

public class Max extends Function {
    public Max() {
        super("max", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return () -> {
            List<Roll> rolls = rollAllSupplier(arguments);
            final RollElement max = rolls.stream()
                    .flatMap(result -> result.getElements().stream())
                    .max(RollElement::compareTo).orElseThrow();

            final ImmutableList<RollElement> res = rolls.stream()
                    .flatMap(result -> result.getElements().stream())
                    .filter(resultElement -> resultElement.compareTo(max) == 0)
                    .collect(ImmutableList.toImmutableList());
            return new Roll(getExpression(getPrimaryName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls));
        };
    }
}

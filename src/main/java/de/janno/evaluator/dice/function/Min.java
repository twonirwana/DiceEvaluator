package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.List;

public class Min extends RollFunction {
    public Min() {
        super("min", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        final RollElement min = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .min(RollElement::compareTo).orElseThrow();

        final ImmutableList<RollElement> res = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .filter(resultElement -> resultElement.compareTo(min) == 0)
                .collect(ImmutableList.toImmutableList());
        return new Roll(getExpression(getPrimaryName(), arguments),
                res,
                arguments.stream()
                        .flatMap(r -> r.getRandomElementsInRoll().stream())
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.copyOf(arguments)
        );
    }
}

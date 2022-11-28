package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Function;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.List;

public class Max extends Function {
    public Max() {
        super("max", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {

        final RollElement max = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .max(RollElement::compareTo).orElseThrow();

        final ImmutableList<RollElement> res = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .filter(resultElement -> resultElement.compareTo(max) == 0)
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

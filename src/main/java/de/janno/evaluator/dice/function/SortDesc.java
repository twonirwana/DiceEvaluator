package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Function;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;

public class SortDesc extends Function {

    public SortDesc() {
        super("desc", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        final ImmutableList<RollElement> res = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .sorted(Comparator.reverseOrder())
                .collect(ImmutableList.toImmutableList());
        return new Roll(getExpression(getPrimaryName(), arguments),
                res,
                arguments.stream()
                        .flatMap(r -> r.getRandomElementsInRoll().stream())
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.copyOf(arguments), null
        );
    }
}

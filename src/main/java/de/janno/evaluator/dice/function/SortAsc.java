package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

public class SortAsc extends Function {

    public SortAsc() {
        super("asc", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        final ImmutableList<RollElement> res = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .sorted()
                .collect(ImmutableList.toImmutableList());
        return new Roll(getExpression(getPrimaryName(), arguments),
                res,
                UniqueRandomElements.from(arguments),
                ImmutableList.copyOf(arguments), null
        );
    }
}

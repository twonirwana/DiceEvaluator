package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Function;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;

public class SortDesc extends Function<Result> {

    public SortDesc() {
        super("desc", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> arguments) throws ExpressionException {
        final ImmutableList<ResultElement> res = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .sorted(Comparator.reverseOrder())
                .collect(ImmutableList.toImmutableList());
        return new Result(getName(),
                res,
                arguments.stream()
                        .flatMap(r -> r.getRandomElementsProducingTheResult().stream())
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.copyOf(arguments)
        );
    }
}

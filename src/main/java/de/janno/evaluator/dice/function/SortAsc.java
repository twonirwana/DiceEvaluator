package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Function;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;

public class SortAsc extends Function<Result> {

    public SortAsc() {
        super("asc", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> arguments) throws ExpressionException {
        final ImmutableList<ResultElement> res = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .sorted()
                .collect(ImmutableList.toImmutableList());
        return new Result(ResultUtil.getExpression(getPrimaryName(), arguments),
                res,
                arguments.stream()
                        .flatMap(r -> r.getRandomElementsProducingTheResult().stream())
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.copyOf(arguments)
        );
    }
}

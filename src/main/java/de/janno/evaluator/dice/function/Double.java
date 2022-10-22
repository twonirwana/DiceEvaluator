package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Function;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;

public class Double extends Function<Result> {

    public Double() {
        super("double", 2);
    }

    @Override
    protected @NonNull Result evaluate(@NonNull List<Result> arguments) throws ExpressionException {
        Result input = arguments.get(0);
        Result toDuplicate = arguments.get(1);

        ImmutableList<ResultElement> resultElements = input.getElements().stream()
                .flatMap(r -> {
                    if (toDuplicate.getElements().contains(r)) {
                        return ImmutableList.of(r, r).stream();
                    } else {
                        return ImmutableList.of(r).stream();
                    }
                })
                .collect(ImmutableList.toImmutableList());

        return new Result(ResultUtil.getExpression(getPrimaryName(), arguments),
                resultElements,
                input.getRandomElementsProducingTheResult(),
                ImmutableList.<Result>builder()
                        .addAll(input.getChildrenResults())
                        .addAll(toDuplicate.getChildrenResults())
                        .build());
    }
}

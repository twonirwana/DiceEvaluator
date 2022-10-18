package de.janno.evaluator.dice.function;

import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Function;
import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;

public class Color extends Function<Result> {
    public Color() {
        super("color", 2);
    }
    @Override
    protected @NonNull Result evaluate(@NonNull List<Result> arguments) throws ExpressionException {
        Result p1 = arguments.get(0);
        Result p2 = arguments.get(1);
        //todo validate
        String color = p2.getElements().get(0).getValue();
        return new Result(ResultUtil.getExpression(getPrimaryName(), arguments),
                p1.getElements().stream()
                        .map(r -> new ResultElement(r.getValue(), color))
                        .collect(ImmutableList.toImmutableList()),
                p1.getRandomElementsProducingTheResult(),
                p1.getChildrenResults());
    }
}

package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Function;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;

public class Cancel extends Function<Result> {
    public Cancel() {
        super("cancel", 3);
    }

    @Override
    protected @NonNull Result evaluate(@NonNull List<Result> arguments) throws ExpressionException {
        Result input = arguments.get(0);
        Result typeA = arguments.get(1);
        Result typeB = arguments.get(2);

        List<ResultElement> noMatch = input.getElements().stream()
                .filter(r -> !typeA.getElements().contains(r) && !typeB.getElements().contains(r))
                .collect(ImmutableList.toImmutableList());
        List<ResultElement> typeAMatch = input.getElements().stream()
                .filter(r -> typeA.getElements().contains(r))
                .collect(ImmutableList.toImmutableList());
        List<ResultElement> typeBMatch = input.getElements().stream()
                .filter(r -> typeB.getElements().contains(r))
                .collect(ImmutableList.toImmutableList());

        ImmutableList.Builder<ResultElement> resultBuilder = ImmutableList.<ResultElement>builder()
                .addAll(noMatch);

        if (typeAMatch.size() > typeBMatch.size()) {
            resultBuilder.addAll(getChancel(typeAMatch, typeBMatch));
        } else if (typeAMatch.size() < typeBMatch.size()) {
            resultBuilder.addAll(getChancel(typeBMatch, typeAMatch));
        }
        return new Result(ResultUtil.getExpression(getPrimaryName(), arguments),
                resultBuilder.build(),
                input.getRandomElementsProducingTheResult(),
                ImmutableList.<Result>builder()
                        .addAll(input.getChildrenResults())
                        .addAll(typeA.getChildrenResults())
                        .addAll(typeB.getChildrenResults())
                        .build());
    }

    private List<ResultElement> getChancel(List<ResultElement> bigger, List<ResultElement> smaller) {
        return bigger.subList(smaller.size(), bigger.size());
    }
}
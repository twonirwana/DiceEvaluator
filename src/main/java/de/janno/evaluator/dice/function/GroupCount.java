package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupCount extends de.janno.evaluator.dice.Function {
    public GroupCount() {
        super("groupC", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        final ImmutableList<RollElement> res = arguments.stream()
                .flatMap(result -> result.getElements().stream())
                .collect(Collectors.groupingBy(Function.identity())).entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<RollElement, List<RollElement>> o) -> o.getValue().size()).reversed())
                .map(entry -> new RollElement("%dx%s".formatted(entry.getValue().size(), entry.getKey().getValue()), entry.getKey().getColor()))
                .collect(ImmutableList.toImmutableList());


        return new Roll(getExpression(getPrimaryName(), arguments),
                res,
                arguments.stream()
                        .flatMap(r -> r.getRandomElementsInRoll().stream())
                        .collect(ImmutableList.toImmutableList()),
                ImmutableList.copyOf(arguments), null);
    }

}
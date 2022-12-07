package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;

public class GroupCount extends de.janno.evaluator.dice.Function {
    public GroupCount() {
        super("groupC", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return () -> {
            List<Roll> rolls = rollAllSupplier(arguments);
            final ImmutableList<RollElement> res = rolls.stream()
                    .flatMap(result -> result.getElements().stream())
                    .collect(Collectors.groupingBy(Function.identity())).entrySet().stream()
                    .sorted(Comparator.comparingInt((Map.Entry<RollElement, List<RollElement>> o) -> o.getValue().size()).reversed())
                    .map(entry -> new RollElement("%dx%s".formatted(entry.getValue().size(), entry.getKey().getValue()), entry.getKey().getColor()))
                    .collect(ImmutableList.toImmutableList());

            return new Roll(getExpression(getPrimaryName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls));
        };
    }

}
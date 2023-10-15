package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class GroupCount extends de.janno.evaluator.dice.Function {
    public GroupCount() {
        super("groupC", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(arguments, variables);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());
            final ImmutableList<RollElement> res = rolls.stream()
                    .flatMap(result -> result.getElements().stream())
                    .collect(Collectors.groupingBy(e -> new ValueAndTag(e.getValue(), e.getTag()))).entrySet().stream()
                    .sorted(Comparator.comparingInt((Map.Entry<ValueAndTag, List<RollElement>> o) -> o.getValue().size()).reversed())
                    .map(groupedElements -> new RollElement("%dx%s".formatted(groupedElements.getValue().size(), groupedElements.getKey().getValue()), groupedElements.getKey().getTag(), RollElement.NO_COLOR))
                    .collect(ImmutableList.toImmutableList());

            return ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls)));
        };
    }

    @Value
    private static class ValueAndTag {
        String value;
        String tag;
    }
}
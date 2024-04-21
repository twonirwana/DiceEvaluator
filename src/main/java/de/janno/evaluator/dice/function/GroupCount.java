package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class GroupCount extends de.janno.evaluator.dice.Function {
    public GroupCount(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("groupC", 1, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                checkRollSize(expressionPosition.getValue(), rolls, getMinArgumentCount(), getMaxArgumentCount());
                final ImmutableList<RollElement> res = rolls.stream()
                        .flatMap(result -> result.getElements().stream())
                        .collect(Collectors.groupingBy(e -> new ValueAndTag(e.getValue(), e.getTag()))).entrySet().stream()
                        .sorted(Comparator.comparingInt((Map.Entry<ValueAndTag, List<RollElement>> o) -> o.getValue().size()).reversed())
                        .map(groupedElements -> new RollElement("%dx%s".formatted(groupedElements.getValue().size(), groupedElements.getKey().getValue()), groupedElements.getKey().getTag(), RollElement.NO_COLOR))
                        .collect(ImmutableList.toImmutableList());

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
                        RandomElementsBuilder.fromRolls(rolls),
                        ImmutableList.copyOf(rolls), maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition.getValue(), arguments);
            }
        };
    }

    @Value
    private static class ValueAndTag {
        String value;
        String tag;
    }
}
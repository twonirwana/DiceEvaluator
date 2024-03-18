package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepLowest extends Operator {

    public KeepLowest(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("l", null, null, Operator.Associativity.LEFT, getOderNumberOf(KeepLowest.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variables) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, variables);
                checkRollSize(inputValue, rolls, 2, 2);

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);
                final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, right, "right"));
                if (rightNumber < 0) {
                    throw new ExpressionException(String.format("The number to keep can not be negativ but was %d", rightNumber));
                }
                final String rightTag = right.getElements().getFirst().getTag();
                ImmutableList<RollElement> otherTagElements = left.getElements().stream()
                        .filter(r -> !r.getTag().equals(rightTag))
                        .collect(ImmutableList.toImmutableList());

                ImmutableList<RollElement> keep = left.getElements().stream()
                        .filter(r -> r.getTag().equals(rightTag))
                        .collect(Collectors.groupingBy(RollElement::getTag)).values().stream()
                        .flatMap(cl -> cl.stream()
                                .sorted()
                                .limit(rightNumber)
                        )
                        .collect(ImmutableList.toImmutableList());
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        ImmutableList.<RollElement>builder()
                                .addAll(keep)
                                .addAll(otherTagElements)
                                .build(),
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(left, right),
                        maxNumberOfElements, keepChildrenRolls)));            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(inputValue, operands);
            }
        };
    }
}

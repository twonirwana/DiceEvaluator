package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepLowest extends Operator {

    public KeepLowest() {
        super("l", null, null, Operator.Associativity.LEFT, getOderNumberOf(KeepLowest.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(operands, variables);
            checkRollSize(inputValue, rolls, 2, 2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, right, "right"));
            if (rightNumber < 0) {
                throw new ExpressionException(String.format("The number to keep can not be negativ but was %d", rightNumber));
            }
            final String rightTag = right.getElements().get(0).getTag();
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
            return Optional.of(ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    ImmutableList.<RollElement>builder()
                            .addAll(keep)
                            .addAll(otherTagElements)
                            .build(),
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right))));
        };
    }
}

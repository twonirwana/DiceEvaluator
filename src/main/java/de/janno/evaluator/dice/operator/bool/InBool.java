package de.janno.evaluator.dice.operator.bool;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class InBool extends Operator {

    public InBool() {
        super("in", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(InBool.class));
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

                boolean isTrue = left.getElements().stream().allMatch(right::isElementsContainsElementWithValueAndTag);

                ImmutableList<RollElement> diceResult = ImmutableList.of(new RollElement(String.valueOf(isTrue), RollElement.NO_TAG, RollElement.NO_COLOR));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        diceResult,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(left, right))));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(getName(), operands);
            }
        };
    }
}

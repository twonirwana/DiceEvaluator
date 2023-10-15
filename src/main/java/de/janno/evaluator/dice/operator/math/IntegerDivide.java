package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class IntegerDivide extends Operator {

    public IntegerDivide() {
        super("/", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(IntegerDivide.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(operands, variables);
            checkRollSize(inputValue, rolls, 2,2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkAllElementsAreSameTag(inputValue, left, right);
            final int leftNumber = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, left, "left"));
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, right, "right"));

            final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(String.valueOf(Math.divideExact(leftNumber, rightNumber)), left.getElements().get(0).getTag(), RollElement.NO_COLOR));
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

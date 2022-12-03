package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkAllElementsAreSameColor;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Divide extends Operator {

    public Divide() {
        super("/", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Divide.class));
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        Roll left = operands.get(0);
        Roll right = operands.get(1);
        checkAllElementsAreSameColor(getName(), left, right);
        final int leftNumber = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
        final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));

        final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(String.valueOf(Math.divideExact(leftNumber, rightNumber)), left.getElements().get(0).getColor()));
        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                res,
                UniqueRandomElements.from(operands),
                ImmutableList.of(left, right), null
        );
    }
}

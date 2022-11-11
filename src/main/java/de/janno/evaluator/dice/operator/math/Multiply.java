package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.RandomElement;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.RollOperator;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkAllElementsAreSameColor;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Multiply extends RollOperator {
    public Multiply() {
        super("*", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Multiply.class));
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        Roll left = operands.get(0);
        Roll right = operands.get(1);
        checkAllElementsAreSameColor(getName(), left, right);
        final int leftNumber = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
        final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(String.valueOf(Math.multiplyExact(leftNumber, rightNumber)), left.getElements().get(0).getColor()));

        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                res,
                ImmutableList.<ImmutableList<RandomElement>>builder()
                        .addAll(left.getRandomElementsInRoll())
                        .addAll(right.getRandomElementsInRoll())
                        .build(),
                ImmutableList.of(left, right)
        );
    }
}

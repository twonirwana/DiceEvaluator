package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Multiply extends Operator {
    public Multiply() {
        super("*", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Multiply.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(getName(), rolls, 2,2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkAllElementsAreSameColor(getName(), left, right);
            final int leftNumber = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(String.valueOf(Math.multiplyExact(leftNumber, rightNumber)), left.getElements().get(0).getColor()));

            return ImmutableList.of(new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

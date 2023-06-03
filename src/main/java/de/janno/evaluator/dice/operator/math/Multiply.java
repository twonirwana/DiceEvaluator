package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Multiply extends Operator {
    public Multiply() {
        super("*", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Multiply.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 2, 2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkAllElementsAreSameTag(inputValue, left, right);
            final BigDecimal leftNumber = left.asDecimal().orElseThrow(() -> throwNotDecimalExpression(inputValue, left, "left"));
            final BigDecimal rightNumber = right.asDecimal().orElseThrow(() -> throwNotDecimalExpression(inputValue, right, "right"));
            final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(leftNumber.multiply(rightNumber).stripTrailingZeros().toPlainString(), left.getElements().get(0).getTag(), RollElement.NO_COLOR));

            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

package de.janno.evaluator.dice.operator.bool;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotDecimalExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class GreaterBool extends Operator {

    public GreaterBool() {
        super(">?", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(GreaterBool.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 2,2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            final BigDecimal leftNumber = left.asDecimal().orElseThrow(() -> throwNotDecimalExpression(inputValue, right, "left"));
            final BigDecimal rightNumber = right.asDecimal().orElseThrow(() -> throwNotDecimalExpression(inputValue, right, "right"));

            ImmutableList<RollElement> diceResult = ImmutableList.of(new RollElement(String.valueOf(leftNumber.compareTo(rightNumber) > 0), RollElement.NO_COLOR));
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    diceResult,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

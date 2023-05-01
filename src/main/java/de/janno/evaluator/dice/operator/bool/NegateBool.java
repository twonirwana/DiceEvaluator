package de.janno.evaluator.dice.operator.bool;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotBoolean;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class NegateBool extends Operator {

    public NegateBool() {
        super("!", OperatorType.UNARY, Associativity.RIGHT, getOderNumberOf(NegateBool.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 1, 1);

            Roll value = rolls.get(0);

            final boolean boolValue = value.asBoolean().orElseThrow(() -> throwNotBoolean(inputValue, value, "right"));

            ImmutableList<RollElement> diceResult = ImmutableList.of(new RollElement(String.valueOf((!boolValue)), RollElement.NO_COLOR));
            return ImmutableList.of(new Roll(getRightUnaryExpression(inputValue, rolls),
                    diceResult,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(value)));
        };
    }
}

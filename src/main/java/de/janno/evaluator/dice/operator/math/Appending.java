package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public final class Appending extends Operator {
    public Appending() {
        super("+", Operator.Associativity.RIGHT, Integer.MAX_VALUE, Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(Appending.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
        return constants -> {

            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(getName(), rolls, 1, 2);

            if (rolls.size() == 1) {
                return ImmutableList.of(rolls.get(0));
            }

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                    .addAll(left.getElements())
                    .addAll(right.getElements())
                    .build();
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(getName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

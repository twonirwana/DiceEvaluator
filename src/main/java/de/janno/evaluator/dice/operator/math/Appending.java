package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;

public final class Appending extends Operator {
    public Appending() {
        super(ImmutableSet.of("+"), Operator.Associativity.RIGHT, Integer.MAX_VALUE, Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(Appending.class));
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> operands) {
        return () -> {
            List<Roll> rolls = rollAllSupplier(operands);
            if (rolls.size() == 1) {
                return rolls.get(0);
            }

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                    .addAll(left.getElements())
                    .addAll(right.getElements())
                    .build();
            return new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right));
        };
    }
}

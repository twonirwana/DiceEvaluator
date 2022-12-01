package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;

public final class Appending extends Operator {
    public Appending() {
        super(ImmutableSet.of("+"), Operator.Associativity.RIGHT, Integer.MAX_VALUE, Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(Appending.class));
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) {
        if (operands.size() == 1) {
            return operands.get(0);
        }

        Roll left = operands.get(0);
        Roll right = operands.get(1);
        final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                .addAll(left.getElements())
                .addAll(right.getElements())
                .build();
        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                res,
                UniqueRandomElements.from(operands),
                ImmutableList.of(left, right), null
        );
    }
}

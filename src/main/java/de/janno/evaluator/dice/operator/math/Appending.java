package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.OperatorOrder;
import de.janno.evaluator.dice.operator.RollOperator;
import lombok.NonNull;

import java.util.List;

public final class Appending extends RollOperator {
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
                ImmutableList.<ImmutableList<RollElement>>builder()
                        .addAll(left.getRandomElementsInRoll())
                        .addAll(right.getRandomElementsInRoll())
                        .build(),
                ImmutableList.of(left, right)
        );
    }
}

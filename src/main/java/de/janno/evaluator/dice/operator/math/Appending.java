package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.RollOperator;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Appending extends RollOperator {
    public Appending() {
        super("+", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Appending.class));
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) {
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

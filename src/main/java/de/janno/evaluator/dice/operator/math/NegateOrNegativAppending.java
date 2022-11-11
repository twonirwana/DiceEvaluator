package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.RandomElement;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.OperatorOrder;
import de.janno.evaluator.dice.operator.RollOperator;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;

public final class NegateOrNegativAppending extends RollOperator {
    public NegateOrNegativAppending() {
        super(ImmutableSet.of("-"), Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(NegateOrNegativAppending.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(NegateOrNegativAppending.class));
    }


    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        if (operands.size() == 1) {
            Roll right = operands.get(0);
            checkContainsOnlyInteger(getName(), right, "right");
            ImmutableList<RollElement> negated = right.getElements().stream()
                    .map(e -> new RollElement(String.valueOf(e.asInteger().orElseThrow() * -1), e.getColor()))
                    .collect(ImmutableList.toImmutableList());
            return new Roll(getRightUnaryExpression(getPrimaryName(), operands),
                    negated,
                    right.getRandomElementsInRoll(),
                    ImmutableList.of(right));
        }

        Roll left = operands.get(0);
        Roll right = operands.get(1);
        checkContainsOnlyInteger(getName(), right, "right");
        final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                .addAll(left.getElements())
                .addAll(right.getElements().stream()
                        .map(e -> new RollElement(String.valueOf(e.asInteger().orElseThrow() * -1), e.getColor()))
                        .toList()
                ).build();

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

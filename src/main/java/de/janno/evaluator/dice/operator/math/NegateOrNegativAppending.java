package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;

public final class NegateOrNegativAppending extends Operator {
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
                    UniqueRandomElements.from(operands),
                    ImmutableList.of(right), null);
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
                UniqueRandomElements.from(operands),
                ImmutableList.of(left, right), null
        );
    }


}

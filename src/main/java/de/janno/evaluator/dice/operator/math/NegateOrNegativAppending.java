package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;

public final class NegateOrNegativAppending extends Operator {
    public NegateOrNegativAppending() {
        super(ImmutableSet.of("-"), Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(NegateOrNegativAppending.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(NegateOrNegativAppending.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            if (rolls.size() == 1) {
                Roll right = rolls.get(0);
                checkContainsOnlyInteger(getName(), right, "right");
                ImmutableList<RollElement> negated = right.getElements().stream()
                        .map(e -> new RollElement(String.valueOf(e.asInteger().orElseThrow() * -1), e.getColor()))
                        .collect(ImmutableList.toImmutableList());
                return new Roll(getRightUnaryExpression(getPrimaryName(), rolls),
                        negated,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(right));
            }

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkContainsOnlyInteger(getName(), right, "right");
            final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                    .addAll(left.getElements())
                    .addAll(right.getElements().stream()
                            .map(e -> new RollElement(String.valueOf(e.asInteger().orElseThrow() * -1), e.getColor()))
                            .toList()
                    ).build();

            return new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right));
        };
    }


}

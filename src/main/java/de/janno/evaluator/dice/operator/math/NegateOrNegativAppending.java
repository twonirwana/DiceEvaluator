package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyDecimal;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public final class NegateOrNegativAppending extends Operator {
    private final static BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);

    public NegateOrNegativAppending() {
        super("-", Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(NegateOrNegativAppending.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(NegateOrNegativAppending.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 1, 2);

            if (rolls.size() == 1) {
                Roll right = rolls.get(0);
                checkContainsOnlyDecimal(inputValue, right, "right");
                ImmutableList<RollElement> negated = right.getElements().stream()
                        .map(e -> new RollElement(e.asDecimal().orElseThrow().multiply(MINUS_ONE).stripTrailingZeros().toPlainString(), e.getTag(), e.getColor()))
                        .collect(ImmutableList.toImmutableList());
                return ImmutableList.of(new Roll(getRightUnaryExpression(inputValue, rolls),
                        negated,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(right)));
            }

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkContainsOnlyDecimal(inputValue, right, "right");
            final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                    .addAll(left.getElements())
                    .addAll(right.getElements().stream()
                            .map(e -> new RollElement(e.asDecimal().orElseThrow().multiply(MINUS_ONE).stripTrailingZeros().toPlainString(), e.getTag(), e.getColor()))
                            .toList()
                    ).build();

            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }


}

package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyDecimal;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public final class NegateOrNegativAddToList extends Operator {
    private final static BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);

    public NegateOrNegativAddToList() {
        super("-", Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(NegateOrNegativAddToList.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(NegateOrNegativAddToList.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(operands, variables);
            checkRollSize(inputValue, rolls, 1, 2);

            if (rolls.size() == 1) {
                Roll right = rolls.getFirst();
                checkContainsOnlyDecimal(inputValue, right, "right");
                ImmutableList<RollElement> negated = right.getElements().stream()
                        .map(e -> new RollElement(e.asDecimal().orElseThrow().multiply(MINUS_ONE).stripTrailingZeros().toPlainString(), e.getTag(), e.getColor()))
                        .collect(ImmutableList.toImmutableList());
                return Optional.of(ImmutableList.of(new Roll(getRightUnaryExpression(inputValue, rolls),
                        negated,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(right))));
            }

            Roll left = rolls.getFirst();
            Roll right = rolls.get(1);
            checkContainsOnlyDecimal(inputValue, right, "right");
            final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                    .addAll(left.getElements())
                    .addAll(right.getElements().stream()
                            .map(e -> new RollElement(e.asDecimal().orElseThrow().multiply(MINUS_ONE).stripTrailingZeros().toPlainString(), e.getTag(), e.getColor()))
                            .toList()
                    ).build();

            return Optional.of(ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right))));
        };
    }


}

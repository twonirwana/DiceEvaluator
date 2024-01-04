package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class LesserEqualThanFilter extends Operator {

    public LesserEqualThanFilter() {
        super("<=", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(LesserEqualThanFilter.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(operands, variables);
            checkRollSize(inputValue, rolls, 2, 2);

            Roll left = rolls.getFirst();
            Roll right = rolls.get(1);
            checkContainsOnlyDecimal(inputValue, left, "left");
            final BigDecimal rightNumber = right.asDecimal().orElseThrow(() -> throwNotDecimalExpression(inputValue, right, "right"));
            ImmutableList<RollElement> diceResult = left.getElements().stream()
                    .filter(i -> i.asDecimal().isPresent() && i.asDecimal().get().compareTo(rightNumber) <= 0
                            //the filter is only applied to elements with the same tag
                            || !Objects.equals(i.getTag(), right.getElements().getFirst().getTag()))
                    .collect(ImmutableList.toImmutableList());
            return Optional.of(ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    diceResult,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right))));
        };
    }
}

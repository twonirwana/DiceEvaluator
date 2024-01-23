package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public final class AddToList extends Operator {
    public AddToList() {
        super("+", Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(AddToList.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(AddToList.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) {
        return variables -> {

            List<Roll> rolls = extendAllBuilder(operands, variables);
            checkRollSize(inputValue, rolls, 1, 2);

            if (rolls.size() == 1) {
                return Optional.of(ImmutableList.of(new Roll(getRightUnaryExpression(inputValue, rolls),
                        rolls.getFirst().getElements(),
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(rolls.getFirst()))));
            }

            Roll left = rolls.getFirst();
            Roll right = rolls.get(1);
            final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                    .addAll(left.getElements())
                    .addAll(right.getElements())
                    .build();
            return Optional.of(ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right))));
        };
    }
}

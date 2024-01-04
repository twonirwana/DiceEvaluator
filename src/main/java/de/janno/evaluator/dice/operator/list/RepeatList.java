package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class RepeatList extends Operator {

    public RepeatList() {
        super("r", null, null, Associativity.LEFT, getOderNumberOf(RepeatList.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> leftRolls = operands.getFirst().extendRoll(variables).orElse(Collections.emptyList());
            checkRollSize(inputValue, leftRolls, 1, 1);
            int left = leftRolls.getFirst().asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, leftRolls.getFirst(), "left"));
            if (left > 10 || left < 0) {
                throw new ExpressionException(String.format("The number of list repeat must between 0-10 but was %d", left));
            }
            if (left == 0) {
                return Optional.empty();
            }

            RollBuilder right = operands.get(1);


            ImmutableList.Builder<Roll> builder = ImmutableList.builder();
            for (int i = 0; i < left; i++) {
                List<Roll> rightRoll = right.extendRoll(variables).orElse(Collections.emptyList());
                checkRollSize(inputValue, rightRoll, 1, 1);
                builder.addAll(rightRoll);
            }
            ImmutableList<Roll> rolls = builder.build();


            return Optional.of(ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, ImmutableList.of(leftRolls.getFirst(), rolls.getFirst())),
                    rolls.stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList()),
                    UniqueRandomElements.from(rolls),
                    rolls)));
        };
    }

}

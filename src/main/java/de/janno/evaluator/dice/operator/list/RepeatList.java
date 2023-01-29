package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class RepeatList extends Operator {

    public RepeatList() {
        super("r", null, null, Associativity.LEFT, getOderNumberOf(RepeatList.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> leftRolls = operands.get(0).extendRoll(constants);
            checkRollSize(inputValue, leftRolls, 1, 1);
            int left = leftRolls.get(0).asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, leftRolls.get(0), "left"));
            if (left > 10 || left < 1) {
                throw new ExpressionException(String.format("The number of repeat must between 1-10 but was %d", left));
            }

            RollBuilder right = operands.get(1);


            ImmutableList.Builder<Roll> builder = ImmutableList.builder();
            for (int i = 0; i < left; i++) {
                builder.addAll(right.extendRoll(new HashMap<>(constants)));
            }
            ImmutableList<Roll> rolls = builder.build();


            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, ImmutableList.of(leftRolls.get(0), rolls.get(0))),
                    rolls.stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList()),
                    UniqueRandomElements.from(rolls),
                    rolls));
        };
    }

}

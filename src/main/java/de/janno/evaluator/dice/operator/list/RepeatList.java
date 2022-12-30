package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class RepeatList extends Operator {

    public RepeatList() {
        //todo better names
        super(Set.of("xl", "XL"), null, null, Associativity.LEFT, getOderNumberOf(RepeatList.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> leftRolls = operands.get(0).extendRoll(constants);
            checkRollSize(getName(), leftRolls, 1, 1);
            int left = leftRolls.get(0).asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), leftRolls.get(0), "left"));
            if (left > 10 || left < 1) {
                throw new ExpressionException(String.format("The number of repeat must between 1-10 but was %d", left));
            }

            RollBuilder right = operands.get(1);


            ImmutableList.Builder<Roll> builder = ImmutableList.builder();
            for (int i = 0; i < left; i++) {
                builder.addAll(right.extendRoll(new HashMap<>(constants)));
            }
            ImmutableList<Roll> rolls = builder.build();


            return ImmutableList.of(new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    rolls.stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList()),
                    UniqueRandomElements.from(rolls),
                    rolls));
        };
    }

}

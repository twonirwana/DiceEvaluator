package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Reroll extends Operator {
    public Reroll() {
        super("rr", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Reroll.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {

            RollBuilder inputBuilder = operands.get(0);
            List<Roll> compareTos = operands.get(1).extendRoll(variables);
            checkRollSize(inputValue, compareTos, 1, 1);
            Roll compareTo = compareTos.get(0);

            List<Roll> rolls = inputBuilder.extendRoll(variables);
            checkRollSize(inputValue, rolls, 1, 1);
            Roll roll = rolls.get(0);
            UniqueRandomElements.Builder builder = UniqueRandomElements.builder();
            builder.add(roll.getRandomElementsInRoll());

            if (roll.getElements().stream().anyMatch(r -> compareTo.getElements().contains(r))) {
                rolls = inputBuilder.extendRoll(variables);
                checkRollSize(inputValue, rolls, 1, 1);
                roll = rolls.get(0);
                builder.add(roll.getRandomElementsInRoll());
            }

            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, ImmutableList.of(roll, compareTo)),
                    roll.getElements(),
                    builder.build(),
                    ImmutableList.<Roll>builder()
                            .addAll(compareTo.getChildrenRolls())
                            .addAll(roll.getChildrenRolls())
                            .build()));

        };
    }

}
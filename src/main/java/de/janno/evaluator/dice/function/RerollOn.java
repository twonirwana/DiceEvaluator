package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class RerollOn extends Function {
    public RerollOn() {
        super("rerollOn", 2);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments) throws ExpressionException {
        return constants -> {

            RollBuilder inputBuilder = arguments.get(0);
            List<Roll> compareTos = arguments.get(1).extendRoll(constants);
            checkRollSize(getName(), compareTos, 1, 1);
            Roll compareTo = compareTos.get(0);

            List<Roll> rolls = inputBuilder.extendRoll(constants);
            checkRollSize(getName(), rolls, 1, 1);
            Roll roll = rolls.get(0);
            UniqueRandomElements.Builder builder = UniqueRandomElements.builder();
            builder.add(roll.getRandomElementsInRoll());

            if (roll.getElements().stream().anyMatch(r -> compareTo.getElements().contains(r))) {
                rolls = inputBuilder.extendRoll(constants);
                checkRollSize(getName(), rolls, 1, 1);
                roll = rolls.get(0);
            }

            return ImmutableList.of(new Roll(getExpression(getPrimaryName(), ImmutableList.of(compareTo, roll)),
                    roll.getElements(),
                    builder.build(),
                    ImmutableList.<Roll>builder()
                            .addAll(compareTo.getChildrenRolls())
                            .addAll(roll.getChildrenRolls())
                            .build()));

        };
    }

}
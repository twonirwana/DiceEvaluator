package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Value extends Function {
    public Value() {
        super("val", 2);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());

            String valName = rolls.get(0).getElements().get(0).getValue();
            if (constants.containsKey(valName)) {
                throw new ExpressionException("The value name '%s' was defined more than once.".formatted(valName));
            }
            constants.put(valName, new Roll(getExpression(inputValue, rolls),
                    rolls.get(1).getElements(),
                    UniqueRandomElements.from(rolls),
                    rolls.get(1).getChildrenRolls()));

            return ImmutableList.of();
        };
    }
}

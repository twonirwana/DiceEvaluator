package de.janno.evaluator.dice.function;

import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;

public class Value extends Function {
    public Value() {
        super("val", 2);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            constants.put(rolls.get(0).getElements().get(0).getValue(), new Roll(getExpression(Value.this.getPrimaryName(), rolls),
                    rolls.get(1).getElements(),
                    UniqueRandomElements.from(rolls),
                    rolls.get(1).getChildrenRolls()));

            return Roll.empty();
        };
    }
}

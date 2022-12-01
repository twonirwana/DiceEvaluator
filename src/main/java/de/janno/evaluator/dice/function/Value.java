package de.janno.evaluator.dice.function;

import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Function;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.UniqueRandomElements;
import lombok.NonNull;

import java.util.List;

public class Value extends Function {
    public Value() {
        super("val", 2);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        return new Roll(getExpression(getPrimaryName(), arguments),
                arguments.get(1).getElements(),
                UniqueRandomElements.from(arguments),
                arguments.get(1).getChildrenRolls(),
                arguments.get(0).getElements().get(0).getValue());
    }
}

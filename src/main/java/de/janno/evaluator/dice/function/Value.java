package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

public class Value extends Function {
    public Value() {
        super("val", 2);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = EvaluationUtils.rollAllSupplier(arguments, constants);
            constants.put(rolls.get(0).getElements().get(0).getValue(), new Roll(getExpression(Value.this.getPrimaryName(), rolls),
                    rolls.get(1).getElements(),
                    UniqueRandomElements.from(rolls),
                    rolls.get(1).getChildrenRolls()));

            //return empty
            return new Roll("", ImmutableList.of(), UniqueRandomElements.empty(), ImmutableList.of());
        };
    }
}

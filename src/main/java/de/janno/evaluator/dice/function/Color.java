package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;

public class Color extends Function {
    public Color() {
        super("color", 2);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = rollAllSupplier(arguments, constants);
            Roll p1 = rolls.get(0);
            Roll p2 = rolls.get(1);
            checkContainsSingleElement(getName(), p2, "second argument");
            String color = p2.getElements().get(0).getValue();
            return new Roll(getExpression(getPrimaryName(), rolls),
                    p1.getElements().stream()
                            .map(r -> new RollElement(r.getValue(), color))
                            .collect(ImmutableList.toImmutableList()),
                    UniqueRandomElements.from(rolls),
                    p1.getChildrenRolls());
        };
    }
}

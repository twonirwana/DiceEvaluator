package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Color extends Function {
    public Color() {
        super("color", 2);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            checkRollSize(getName(), rolls, getMinArgumentCount(), getMaxArgumentCount());
            Roll p1 = rolls.get(0);
            Roll p2 = rolls.get(1);
            checkContainsSingleElement(getName(), p2, "second argument");
            String color = p2.getElements().get(0).getValue();
            return ImmutableList.of(new Roll(getExpression(getName(), rolls),
                    p1.getElements().stream()
                            .map(r -> new RollElement(r.getValue(), color))
                            .collect(ImmutableList.toImmutableList()),
                    UniqueRandomElements.from(rolls),
                    p1.getChildrenRolls()));
        };
    }
}

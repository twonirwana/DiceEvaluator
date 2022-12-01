package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;

public class Color extends Function {
    public Color() {
        super("color", 2);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll p1 = arguments.get(0);
        Roll p2 = arguments.get(1);
        checkContainsSingleElement(getName(), p2, "second argument");
        String color = p2.getElements().get(0).getValue();
        return new Roll(getExpression(getPrimaryName(), arguments),
                p1.getElements().stream()
                        .map(r -> new RollElement(r.getValue(), color))
                        .collect(ImmutableList.toImmutableList()),
                UniqueRandomElements.from(arguments),
                p1.getChildrenRolls(), null);
    }
}

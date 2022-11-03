package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.Roll;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;

public class IfLesser extends RollFunction {
    public IfLesser() {
        super("ifL", 3, 4);
    }

    @Override
    protected @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll input = arguments.get(0);
        Roll compareTo = arguments.get(1);
        Roll trueResult = arguments.get(2);
        final Roll falseResult;
        if (arguments.size() == 3) {
            falseResult = input;
        } else {
            falseResult = arguments.get(3);
        }
        checkContainsSingleElement(getName(), input, "first argument");
        checkContainsSingleElement(getName(), compareTo, "second argument");

        final Roll result;

        if (input.getElements().get(0).compareTo(compareTo.getElements().get(0)) < 0) {
            result = trueResult;
        } else {
            result = falseResult;
        }

        return new Roll(getExpression(getPrimaryName(), arguments),
                result.getElements(),
                input.getRandomElementsInRoll(),
                ImmutableList.<Roll>builder()
                        .addAll(input.getChildrenRolls())
                        .addAll(result.getChildrenRolls())
                        .build());
    }

}
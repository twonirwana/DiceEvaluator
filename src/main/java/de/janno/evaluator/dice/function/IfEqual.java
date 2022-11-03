package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.Roll;
import lombok.NonNull;

import java.util.List;

public class IfEqual extends RollFunction {
    public IfEqual() {
        super("ifE", 3, 4);
    }

    @Override
    protected @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll input = arguments.get(0);
        Roll equal = arguments.get(1);
        Roll trueResult = arguments.get(2);
        final Roll falseResult;
        if (arguments.size() == 3) {
            falseResult = input;
        } else {
            falseResult = arguments.get(3);
        }

        final Roll result;
        if (input.getElements().equals(equal.getElements())) {
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
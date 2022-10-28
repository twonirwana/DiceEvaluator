package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.Roll;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;

public class IfGreater extends RollFunction {
    public IfGreater() {
        super("ifG", 4);
    }

    @Override
    protected @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll input = arguments.get(0);
        Roll compareTo = arguments.get(1);
        Roll trueResult = arguments.get(2);
        Roll falseResult = arguments.get(3);

        final int inputNumber = input.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), input, "first argument"));
        final int compareToNumber = compareTo.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), compareTo, "second argument"));
        final Roll result;

        if (inputNumber > compareToNumber) {
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
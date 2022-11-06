package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.List;

public abstract class AbstractIf extends RollFunction {

    public AbstractIf(@NonNull String name) {
        super(name, 3, Integer.MAX_VALUE);
    }

    @Override
    protected @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll input = arguments.get(0);

        int counter = 1;
        while (counter < arguments.size() - 1) {
            Roll compareTo = arguments.get(counter);
            Roll trueResult = arguments.get(counter + 1);

            if (compare(input, counter, compareTo, counter + 1)) {
                return new Roll(getExpression(getPrimaryName(), arguments),
                        trueResult.getElements(),
                        ImmutableList.<ImmutableList<RollElement>>builder()
                                .addAll(input.getRandomElementsInRoll())
                                .addAll(trueResult.getRandomElementsInRoll())
                                .build(),
                        ImmutableList.<Roll>builder()
                                .addAll(input.getChildrenRolls())
                                .addAll(trueResult.getChildrenRolls())
                                .build());
            }
            counter += 2;
        }

        final Roll result;
        final ImmutableList<ImmutableList<RollElement>> randomElementsInRoll;
        if (counter == arguments.size()) {
            result = arguments.get(0);
            randomElementsInRoll = input.getRandomElementsInRoll();
        } else {
            result = arguments.get(arguments.size() - 1);
            randomElementsInRoll = ImmutableList.<ImmutableList<RollElement>>builder()
                    .addAll(input.getRandomElementsInRoll())
                    .addAll(result.getRandomElementsInRoll())
                    .build();
        }
        return new Roll(getExpression(getPrimaryName(), arguments),
                result.getElements(),
                randomElementsInRoll,
                ImmutableList.<Roll>builder()
                        .addAll(input.getChildrenRolls())
                        .addAll(result.getChildrenRolls())
                        .build());
    }


    protected abstract boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) throws ExpressionException;
}

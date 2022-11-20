package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.RandomElement;
import de.janno.evaluator.dice.Roll;
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
        ImmutableList.Builder<ImmutableList<RandomElement>> randomElements = ImmutableList.builder();
        randomElements.addAll(input.getRandomElementsInRoll());
        while (counter < arguments.size() - 1) {
            Roll compareTo = arguments.get(counter);
            Roll trueResult = arguments.get(counter + 1);
            if (!compareTo.getRandomElementsInRoll().isEmpty()) {
                randomElements.addAll(compareTo.getRandomElementsInRoll());
            }
            if (compare(input, counter, compareTo, counter + 1)) {
                randomElements.addAll(trueResult.getRandomElementsInRoll());
                return new Roll(getExpression(getPrimaryName(), arguments),
                        trueResult.getElements(),
                        randomElements.build(),
                        ImmutableList.<Roll>builder()
                                .addAll(input.getChildrenRolls())
                                .addAll(trueResult.getChildrenRolls())
                                .build());
            }
            counter += 2;
        }

        final Roll result;
        //there is a last element in the arguments, which is the default result
        if (counter != arguments.size())  {
            result = arguments.get(arguments.size() - 1);
            randomElements.addAll(result.getRandomElementsInRoll());
        } else {
            //if there is no default result, the result is the input
            result = input;
        }
        return new Roll(getExpression(getPrimaryName(), arguments),
                result.getElements(),
                randomElements.build(),
                ImmutableList.<Roll>builder()
                        .addAll(input.getChildrenRolls())
                        .addAll(result.getChildrenRolls())
                        .build());
    }


    protected abstract boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) throws ExpressionException;
}

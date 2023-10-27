package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public abstract class AbstractIf extends Function {

    public AbstractIf(@NonNull String name) {
        super(name, 3, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(arguments, variables);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());
            Roll input = rolls.get(0);

            int counter = 1;
            UniqueRandomElements.Builder randomElements = UniqueRandomElements.builder();
            randomElements.add(input.getRandomElementsInRoll());
            while (counter < rolls.size() - 1) {
                Roll compareTo = rolls.get(counter);
                Roll trueResult = rolls.get(counter + 1);
                randomElements.add(compareTo.getRandomElementsInRoll());
                if (compare(input, counter, compareTo, counter + 1)) {
                    randomElements.add(trueResult.getRandomElementsInRoll());
                    return Optional.of(ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                            trueResult.getElements(),
                            randomElements.build(),
                            ImmutableList.<Roll>builder()
                                    .addAll(input.getChildrenRolls())
                                    .addAll(trueResult.getChildrenRolls())
                                    .build())));
                }
                counter += 2;
            }

            final Roll result;
            //there is a last element in the arguments, which is the default result
            if (counter != rolls.size()) {
                result = rolls.get(rolls.size() - 1);
                randomElements.add(result.getRandomElementsInRoll());
            } else {
                //if there is no default result, the result is the input
                result = input;
            }
            return Optional.of(ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                    result.getElements(),
                    randomElements.build(),
                    ImmutableList.<Roll>builder()
                            .addAll(input.getChildrenRolls())
                            .addAll(result.getChildrenRolls())
                            .build())));
        };
    }


    protected abstract boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) throws ExpressionException;
}

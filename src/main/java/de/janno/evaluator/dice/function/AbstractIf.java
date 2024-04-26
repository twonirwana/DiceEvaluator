package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public abstract class AbstractIf extends Function {

    public AbstractIf(@NonNull String name, int maxNumberOfElements, boolean keepChildrenRolls) {
        super(name, 3, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {

        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                checkRollSize(expressionPosition, rolls, getMinArgumentCount(), getMaxArgumentCount());
                Roll input = rolls.getFirst();

                int counter = 1;
                RandomElementsBuilder randomElementsBuilder = RandomElementsBuilder.ofRoll(input);
                while (counter < rolls.size() - 1) {
                    Roll compareTo = rolls.get(counter);
                    Roll trueResult = rolls.get(counter + 1);
                    randomElementsBuilder.addRoll(compareTo);
                    if (compare(input, counter, compareTo, counter + 1, expressionPosition)) {
                        randomElementsBuilder.addRoll(trueResult);
                        return Optional.of(ImmutableList.of(new Roll(toExpression(),
                                trueResult.getElements(),
                                randomElementsBuilder.build(),
                                ImmutableList.<Roll>builder()
                                        .addAll(input.getChildrenRolls())
                                        .addAll(trueResult.getChildrenRolls())
                                        .build(), maxNumberOfElements, keepChildrenRolls)));
                    }
                    counter += 2;
                }

                final Roll result;
                //there is a last element in the arguments, which is the default result
                if (counter != rolls.size()) {
                    result = rolls.getLast();
                    randomElementsBuilder.addRoll(result);
                } else {
                    //if there is no default result, the result is the input
                    result = input;
                }
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        result.getElements(),
                        randomElementsBuilder.build(),
                        ImmutableList.<Roll>builder()
                                .addAll(input.getChildrenRolls())
                                .addAll(result.getChildrenRolls())
                                .build(), maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition, arguments);
            }
        };
    }


    protected abstract boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition, ExpressionPosition expressionPosition) throws ExpressionException;
}

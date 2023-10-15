package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotBoolean;

public class If extends Function {
    public If() {
        super("if", 2, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(arguments, variables);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());

            final AtomicInteger counter = new AtomicInteger(0);
            UniqueRandomElements.Builder randomElements = UniqueRandomElements.builder();
            while (counter.get() < rolls.size() - 1) {
                Roll booleanExpression = rolls.get(counter.get());
                final boolean booleanValue = booleanExpression.asBoolean()
                        .orElseThrow(() -> throwNotBoolean(inputValue, booleanExpression, "position %d".formatted(counter.get())));

                Roll trueResult = rolls.get(counter.get() + 1);
                randomElements.add(booleanExpression.getRandomElementsInRoll());
                if (booleanValue) {
                    randomElements.add(trueResult.getRandomElementsInRoll());
                    return ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                            trueResult.getElements(),
                            randomElements.build(),
                            ImmutableList.<Roll>builder()
                                    .addAll(booleanExpression.getChildrenRolls())
                                    .addAll(trueResult.getChildrenRolls())
                                    .build()));
                }
                counter.addAndGet(2);
            }

            final Roll result;
            //there is a last element in the arguments, which is the default result
            if (counter.get() != rolls.size()) {
                result = rolls.get(rolls.size() - 1);
                randomElements.add(result.getRandomElementsInRoll());
                return ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                        result.getElements(),
                        randomElements.build(),
                        ImmutableList.<Roll>builder()
                                .addAll(result.getChildrenRolls())
                                .build()));

            } else {
                //if there is no default result, the result is empty
                return ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                        ImmutableList.of(),
                        randomElements.build(),
                        ImmutableList.of()));
            }

        };


    }
}
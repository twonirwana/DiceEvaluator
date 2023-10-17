package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotBoolean;

public class If extends Function {
    public If() {
        super("if", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {


            ImmutableList.Builder<Roll> allRolls = ImmutableList.builder();
            RollBuilder.RollsAndIndex next = RollBuilder.getNextNonEmptyRolls(arguments, 0, variables);
            if (next.getRolls().isEmpty()) {
                throw new ExpressionException(String.format("'%s' requires as %s inputs but was empty", inputValue, 1));
            }
            UniqueRandomElements.Builder booleanRandomElements = UniqueRandomElements.builder();
            Map<String, Roll> trueVariable = new ConcurrentHashMap<>(variables);
            RollBuilder.RollsAndIndex after = RollBuilder.getNextNonEmptyRolls(arguments, next.getIndex() + 1, trueVariable);

            if (after.getRolls().isEmpty()) {
                throw new ExpressionException(String.format("'%s' requires as %s inputs but was empty", inputValue, 2));
            }

            while (after.getRolls().isPresent()) {
                checkRollSize(inputValue, next.getRolls().get(), 1, 1);
                allRolls.addAll(next.getRolls().get());
                allRolls.addAll(after.getRolls().get());
                Roll booleanExpression = next.getRolls().get().get(0);
                int nextIndex = next.getIndex();
                final boolean booleanValue = booleanExpression.asBoolean()
                        .orElseThrow(() -> throwNotBoolean(inputValue, booleanExpression, "position %d".formatted(nextIndex)));
                booleanRandomElements.add(booleanExpression.getRandomElementsInRoll());
                if (booleanValue) {
                    List<Roll> trueResult = after.getRolls().get();
                    variables.putAll(trueVariable);
                    UniqueRandomElements allBooleanRandomElements = booleanRandomElements.build();
                    return Optional.of(trueResult.stream()
                            .map(r -> new Roll(getExpression(inputValue, allRolls.build()), r.getElements(),
                                    UniqueRandomElements.builder()
                                            .add(allBooleanRandomElements)
                                            .add(r.getRandomElementsInRoll())
                                            .build(),
                                    ImmutableList.<Roll>builder()
                                            .addAll(booleanExpression.getChildrenRolls())
                                            .addAll(r.getChildrenRolls())
                                            .build()))
                            .collect(ImmutableList.toImmutableList()));
                }
                next = RollBuilder.getNextNonEmptyRolls(arguments, after.getIndex() + 1, variables);
                trueVariable = new ConcurrentHashMap<>(variables); //reset the true variables
                after = RollBuilder.getNextNonEmptyRolls(arguments, next.getIndex() + 1, trueVariable);
            }

            //there is a last element in the arguments, which is the default result
            if (next.getRolls().isPresent()) {
                List<Roll> defaultResult = next.getRolls().get();
                allRolls.addAll(defaultResult);
                variables.putAll(trueVariable); // if there was a result but it was empty (only val) then we need to take the val values
                return Optional.of(defaultResult.stream()
                        .map(r -> new Roll(getExpression(inputValue, allRolls.build()), r.getElements(),
                                UniqueRandomElements.builder()
                                        .add(booleanRandomElements.build())
                                        .add(r.getRandomElementsInRoll())
                                        .build(), r.getChildrenRolls()))
                        .collect(ImmutableList.toImmutableList()));

            } else {
                return Optional.empty();
            }

        };


    }
}
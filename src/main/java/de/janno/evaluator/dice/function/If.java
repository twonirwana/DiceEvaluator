package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Collections;
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
            if (arguments.size() < 2) {
                throw new ExpressionException(String.format("'%s' requires as 2 inputs but was '%s'", getName(), arguments.size()));
            }
            ImmutableList.Builder<Roll> allRolls = ImmutableList.builder();
            Optional<List<Roll>> checkIfTrue = arguments.getFirst().extendRoll(variables);
            if (checkIfTrue.isEmpty()) {
                throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", inputValue));
            }
            UniqueRandomElements.Builder booleanRandomElements = UniqueRandomElements.builder();
            Map<String, Roll> trueVariable = new ConcurrentHashMap<>(variables);
            Optional<List<Roll>> returnIfTrue = arguments.get(1).extendRoll(trueVariable);

            int checkIfTrueIndex = 1;
            while (checkIfTrueIndex < arguments.size()) {
                if (checkIfTrue.isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as %s argument", inputValue, checkIfTrueIndex));
                }
                checkRollSize(inputValue, checkIfTrue.get(), 1, 1);
                Roll booleanExpression = checkIfTrue.get().getFirst();
                allRolls.addAll(checkIfTrue.get());
                allRolls.addAll(returnIfTrue.orElse(Collections.emptyList()));

                int booleanArgumentIndex = checkIfTrueIndex;
                final boolean booleanValue = booleanExpression.asBoolean()
                        .orElseThrow(() -> throwNotBoolean(inputValue, booleanExpression, "position %d".formatted(booleanArgumentIndex)));
                booleanRandomElements.add(booleanExpression.getRandomElementsInRoll());
                if (booleanValue) {
                    List<Roll> trueResult = returnIfTrue.orElse(Collections.emptyList());
                    variables.putAll(trueVariable); //only the variable of the true result are added
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
                checkIfTrueIndex = checkIfTrueIndex + 2;
                if (checkIfTrueIndex < arguments.size()) {
                    checkIfTrue = arguments.get(checkIfTrueIndex - 1).extendRoll(variables);
                    trueVariable = new ConcurrentHashMap<>(variables); //reset the true variables, we only add them to the result if the result was returned
                    returnIfTrue = arguments.get(checkIfTrueIndex).extendRoll(trueVariable);
                } else {
                    //should not be read anymore, but making sure
                    checkIfTrue = Optional.empty();
                    returnIfTrue = Optional.empty();
                }

            }

            //there is a last element in the arguments, which is the default result and not a check value
            if (checkIfTrueIndex == arguments.size()) {
                Optional<List<Roll>> defaultResult = arguments.get(checkIfTrueIndex - 1).extendRoll(variables);
                if (defaultResult.isPresent()) {
                    allRolls.addAll(defaultResult.get());
                    return Optional.of(defaultResult.get().stream()
                            .map(r -> new Roll(getExpression(inputValue, allRolls.build()), r.getElements(),
                                    UniqueRandomElements.builder()
                                            .add(booleanRandomElements.build())
                                            .add(r.getRandomElementsInRoll())
                                            .build(), r.getChildrenRolls()))
                            .collect(ImmutableList.toImmutableList()));
                }
            }

            return Optional.empty();

        };


    }
}
package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotBoolean;

public class If extends Function {
    public If(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("if", 1, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {

        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                if (arguments.size() < 2) {
                    throw new ExpressionException(String.format("'%s' requires as 2 inputs but was '%s'", getName(), arguments.size()));
                }
                ImmutableList.Builder<Roll> allRolls = ImmutableList.builder();
                Optional<List<Roll>> checkIfTrue = arguments.getFirst().extendRoll(rollContext);
                if (checkIfTrue.isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", expressionPosition.value()));
                }
                UniqueRandomElements.Builder booleanRandomElements = UniqueRandomElements.builder();
                //todo why?
                RollContext trueContext = rollContext.copy();
                Optional<List<Roll>> returnIfTrue = arguments.get(1).extendRoll(trueContext);

                int checkIfTrueIndex = 1;
                while (checkIfTrueIndex < arguments.size()) {
                    if (checkIfTrue.isEmpty()) {
                        throw new ExpressionException(String.format("'%s' requires a non-empty input as %s argument", expressionPosition.value(), checkIfTrueIndex));
                    }
                    checkRollSize(expressionPosition.value(), checkIfTrue.get(), 1, 1);
                    Roll booleanExpression = checkIfTrue.get().getFirst();
                    allRolls.addAll(checkIfTrue.get());
                    allRolls.addAll(returnIfTrue.orElse(Collections.emptyList()));

                    int booleanArgumentIndex = checkIfTrueIndex;
                    final boolean booleanValue = booleanExpression.asBoolean()
                            .orElseThrow(() -> throwNotBoolean(expressionPosition.value(), booleanExpression, "position %d".formatted(booleanArgumentIndex)));
                    booleanRandomElements.add(booleanExpression.getRandomElementsInRoll());
                    if (booleanValue) {
                        List<Roll> trueResult = returnIfTrue.orElse(Collections.emptyList());
                        rollContext.merge(trueContext); //only the variable of the true result are added
                        UniqueRandomElements allBooleanRandomElements = booleanRandomElements.build();
                        ImmutableList.Builder<Roll> resultBuilder = ImmutableList.builder();
                        for (Roll r : trueResult) {
                            resultBuilder.add(new Roll(toExpression(), r.getElements(),
                                    UniqueRandomElements.builder()
                                            .add(allBooleanRandomElements)
                                            .add(r.getRandomElementsInRoll())
                                            .build(),
                                    ImmutableList.<Roll>builder()
                                            .addAll(booleanExpression.getChildrenRolls())
                                            .addAll(r.getChildrenRolls())
                                            .build(),
                                    maxNumberOfElements, keepChildrenRolls));
                        }
                        return Optional.of(resultBuilder.build());
                    }
                    checkIfTrueIndex = checkIfTrueIndex + 2;
                    if (checkIfTrueIndex < arguments.size()) {
                        checkIfTrue = arguments.get(checkIfTrueIndex - 1).extendRoll(rollContext);
                        trueContext = rollContext.copy(); //reset the true context, we only add them to the result if the result was returned
                        returnIfTrue = arguments.get(checkIfTrueIndex).extendRoll(trueContext);
                    } else {
                        //should not be read anymore, but making sure
                        checkIfTrue = Optional.empty();
                        returnIfTrue = Optional.empty();
                    }

                }

                //there is a last element in the arguments, which is the default result and not a check value
                if (checkIfTrueIndex == arguments.size()) {
                    Optional<List<Roll>> defaultResult = arguments.get(checkIfTrueIndex - 1).extendRoll(rollContext);
                    if (defaultResult.isPresent()) {
                        allRolls.addAll(defaultResult.get());

                        ImmutableList.Builder<Roll> resultBuilder = ImmutableList.builder();
                        for (Roll r : defaultResult.get()) {
                            resultBuilder.add(new Roll(toExpression(), r.getElements(),
                                    UniqueRandomElements.builder()
                                            .add(booleanRandomElements.build())
                                            .add(r.getRandomElementsInRoll())
                                            .build(), r.getChildrenRolls(),
                                    maxNumberOfElements, keepChildrenRolls));
                        }
                        return Optional.of(resultBuilder.build());
                    }
                }

                return Optional.empty();
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition.value(), arguments);
            }
        };


    }
}
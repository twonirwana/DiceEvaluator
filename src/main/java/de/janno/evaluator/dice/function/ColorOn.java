package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsNoOrSingleElement;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class ColorOn extends Function {
    public ColorOn(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("colorOn", 3, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                if (arguments.size() % 2 == 0) {
                    throw new ExpressionException(String.format("'%s' requires an odd number of arguments but was %d", getName(), arguments.size()), expressionPosition);
                }
                Optional<List<Roll>> input = arguments.getFirst().extendRoll(rollContext);
                if (input.isEmpty()) {
                    return Optional.of(List.of());
                }
                ImmutableList.Builder<Roll> allRolls = ImmutableList.<Roll>builder().addAll(input.get());
                ImmutableList<RollElement> inputRollElements = input.get().stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList());
                for (int i = 1; i < arguments.size() - 1; i = i + 2) {

                    Optional<List<Roll>> inRolls = arguments.get(i).extendRoll(rollContext);
                    checkRollSize(expressionPosition, inRolls, 1, 1);
                    final Roll inRoll = inRolls.orElseThrow().getFirst();
                    allRolls.add(inRoll);

                    ImmutableList.Builder<RollElement> currentIterationElements = ImmutableList.builder();
                    for (RollElement rollElement : inputRollElements) {
                        if (inRoll.isElementsContainsElementWithValueAndTag(rollElement)) {
                            Optional<List<Roll>> colorRolls = arguments.get(i + 1).extendRoll(rollContext);
                            checkRollSize(expressionPosition, colorRolls, 1, 1);
                            Roll colorRoll = colorRolls.orElseThrow().getFirst();
                            checkContainsNoOrSingleElement(expressionPosition, colorRoll, "%d argument".formatted(i + 1));
                            final String color = colorRoll.asSingleValue().orElse(RollElement.NO_COLOR);
                            allRolls.add(colorRoll);
                            currentIterationElements.add(new RollElement(rollElement.getValue(), rollElement.getTag(), color));
                        } else {
                            currentIterationElements.add(rollElement);
                        }
                    }
                    inputRollElements = currentIterationElements.build();
                }

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        inputRollElements,
                        RandomElementsBuilder.fromRolls(allRolls.build(), rollContext),
                        allRolls.build(),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls)));

            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition, arguments);
            }
        };
    }
}
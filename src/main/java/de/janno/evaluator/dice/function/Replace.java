package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Replace extends Function {
    public Replace() {
        super("replace", 3, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variables) throws ExpressionException {
                if (arguments.size() % 2 == 0) {
                    throw new ExpressionException(String.format("'%s' requires an odd number of arguments but was %d", getName(), arguments.size()));
                }
                Optional<List<Roll>> input = arguments.getFirst().extendRoll(variables);
                if (input.isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", inputValue));
                }
                ImmutableList.Builder<Roll> allRolls = ImmutableList.builder();
                ImmutableList.Builder<Roll> rollExpression = ImmutableList.builder();
                allRolls.addAll(input.get());
                rollExpression.addAll(input.get());
                ImmutableList<RollElement> rollElements = input.get().stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList());
                ImmutableList.Builder<Roll> childrenRollBuilder = ImmutableList.<Roll>builder()
                        .addAll(input.get());
                for (int i = 1; i < arguments.size() - 1; i = i + 2) {
                    Optional<List<Roll>> find = arguments.get(i).extendRoll(variables);
                    find.ifPresent(allRolls::addAll);
                    if (find.isPresent() && rollElements.stream().anyMatch(r -> find.get().stream().anyMatch(f -> f.isElementsContainsElementWithValueAndTag(r)))) {
                        childrenRollBuilder.addAll(find.get());
                        RollBuilder replaceArgument = arguments.get(i + 1);
                        ImmutableList.Builder<RollElement> newRollElementsList = ImmutableList.builder();
                        for (RollElement r : rollElements) {
                            if (find.get().stream().anyMatch(f -> f.isElementsContainsElementWithValueAndTag(r))) {
                                Optional<List<Roll>> replace = replaceArgument.extendRoll(variables);
                                replace.ifPresent(allRolls::addAll);
                                if (replace.isPresent()) {
                                    childrenRollBuilder.addAll(replace.get());
                                    newRollElementsList.addAll(replace.get().stream().flatMap(re -> re.getElements().stream()).toList());
                                } else {
                                    newRollElementsList.add(r);
                                }
                            } else {
                                newRollElementsList.add(r);
                            }
                        }
                        rollElements = newRollElementsList.build();
                    }
                }


                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        rollElements,
                        UniqueRandomElements.from(allRolls.build()),
                        childrenRollBuilder.build())));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(inputValue, arguments);
            }
        };
    }
}
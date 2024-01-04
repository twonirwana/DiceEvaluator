package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Replace extends Function {
    public Replace() {
        super("replace", 3, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(arguments, variables);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());
            if (rolls.size() % 2 == 0) {
                throw new ExpressionException(String.format("'%s' an odd number of arguments but was %d", getName(), arguments.size()));
            }
            Roll input = rolls.getFirst();
            ImmutableList<RollElement> rollElements = input.getElements();
            ImmutableList.Builder<Roll> childrenRollBuilder = ImmutableList.<Roll>builder()
                    .addAll(input.getChildrenRolls());
            for (int i = 1; i < rolls.size() - 1; i = i + 2) {
                Roll find = rolls.get(i);
                Roll replace = rolls.get(i + 1);
                childrenRollBuilder.addAll(find.getChildrenRolls())
                        .addAll(replace.getChildrenRolls());
                rollElements = rollElements.stream()
                        .flatMap(r -> {
                            if (find.isElementsContainsElementWithValueAndTag(r)) {
                                return replace.getElements().stream();
                            }
                            return Stream.of(r);
                        })
                        .collect(ImmutableList.toImmutableList());
            }


            return Optional.of(ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                    rollElements,
                    UniqueRandomElements.from(rolls),
                    childrenRollBuilder.build())));
        };
    }
}
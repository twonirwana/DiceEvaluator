package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Stream;

public class Replace extends Function {
    public Replace() {
        super("replace", 3);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll input = arguments.get(0);
        Roll find = arguments.get(1);
        Roll replace = arguments.get(2);

        ImmutableList<RollElement> rollElements = input.getElements().stream()
                .flatMap(r -> {
                    if (find.getElements().contains(r)) {
                        return replace.getElements().stream();
                    }
                    return Stream.of(r);
                })
                .collect(ImmutableList.toImmutableList());


        return new Roll(getExpression(getPrimaryName(), arguments),
                rollElements,
                UniqueRandomElements.from(arguments),
                ImmutableList.<Roll>builder()
                        .addAll(input.getChildrenRolls())
                        .addAll(find.getChildrenRolls())
                        .addAll(replace.getChildrenRolls())
                        .build(), null);
    }
}
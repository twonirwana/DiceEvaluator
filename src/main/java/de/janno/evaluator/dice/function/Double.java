package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Function;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.List;

public class Double extends Function {

    public Double() {
        super("double", 2);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll input = arguments.get(0);
        Roll toDuplicate = arguments.get(1);

        ImmutableList<RollElement> rollElements = input.getElements().stream()
                .flatMap(r -> {
                    if (toDuplicate.getElements().contains(r)) {
                        return ImmutableList.of(r, r).stream();
                    } else {
                        return ImmutableList.of(r).stream();
                    }
                })
                .collect(ImmutableList.toImmutableList());

        return new Roll(getExpression(getPrimaryName(), arguments),
                rollElements,
                input.getRandomElementsInRoll(),
                ImmutableList.<Roll>builder()
                        .addAll(input.getChildrenRolls())
                        .addAll(toDuplicate.getChildrenRolls())
                        .build());
    }
}

package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

/**
 * Deprecated in favor of the mightier 'replace' function.
 */
@Deprecated
public class Double extends Function {

    public Double() {
        super("double", 2);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            checkRollSize(getName(), rolls, getMinArgumentCount(), getMaxArgumentCount());
            Roll input = rolls.get(0);
            Roll toDuplicate = rolls.get(1);

            ImmutableList<RollElement> rollElements = input.getElements().stream()
                    .flatMap(r -> {
                        if (toDuplicate.getElements().contains(r)) {
                            return ImmutableList.of(r, r).stream();
                        } else {
                            return ImmutableList.of(r).stream();
                        }
                    })
                    .collect(ImmutableList.toImmutableList());

            return ImmutableList.of(new Roll(getExpression(getPrimaryName(), rolls),
                    rollElements,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.<Roll>builder()
                            .addAll(input.getChildrenRolls())
                            .addAll(toDuplicate.getChildrenRolls())
                            .build()));
        };
    }
}

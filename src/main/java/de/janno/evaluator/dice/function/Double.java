package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;

/**
 * Deprecated in favor of the mightier 'replace' function.
 */
@Deprecated
public class Double extends Function {

    public Double() {
        super("double", 2);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = rollAllSupplier(arguments, constants);
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

            return new Roll(getExpression(getPrimaryName(), rolls),
                    rollElements,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.<Roll>builder()
                            .addAll(input.getChildrenRolls())
                            .addAll(toDuplicate.getChildrenRolls())
                            .build());
        };
    }
}

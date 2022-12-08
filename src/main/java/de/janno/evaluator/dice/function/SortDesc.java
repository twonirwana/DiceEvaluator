package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;

public class SortDesc extends Function {

    public SortDesc() {
        super("desc", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = rollAllSupplier(arguments, constants);
            final ImmutableList<RollElement> res = rolls.stream()
                    .flatMap(result -> result.getElements().stream())
                    .sorted(Comparator.reverseOrder())
                    .collect(ImmutableList.toImmutableList());
            return new Roll(getExpression(getPrimaryName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls));
        };
    }
}

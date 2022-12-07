package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;

public class Concat extends Function {
    public Concat() {
        super("concat", 2, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return () -> {
            List<Roll> rolls = rollAllSupplier(arguments);
            String joined = rolls.stream()
                    .map(Roll::getResultString)
                    .collect(Collectors.joining());
            return new Roll(getExpression(getPrimaryName(), rolls),
                    ImmutableList.of(new RollElement(joined, RollElement.NO_COLOR)),
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls));
        };
    }
}

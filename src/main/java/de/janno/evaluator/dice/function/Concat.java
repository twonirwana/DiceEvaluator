package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class Concat extends Function {
    public Concat() {
        super("concat", 2, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        String joined = arguments.stream()
                .map(Roll::getResultString)
                .collect(Collectors.joining());
        return new Roll(getExpression(getPrimaryName(), arguments),
                ImmutableList.of(new RollElement(joined, RollElement.NO_COLOR)),
                UniqueRandomElements.from(arguments),
                ImmutableList.copyOf(arguments), null
        );
    }
}

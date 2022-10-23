package de.janno.evaluator.dice.function;

import de.janno.evaluator.Function;
import de.janno.evaluator.dice.Roll;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RollFunction extends Function<Roll> {
    public RollFunction(@NonNull String name, int argumentCount) {
        super(name, argumentCount);
    }

    public RollFunction(@NonNull String name, int minArgumentCount, int maxArgumentCount) {
        super(name, minArgumentCount, maxArgumentCount);
    }

    public RollFunction(@NonNull Set<String> names, int minArgumentCount, int maxArgumentCount) {
        super(names, minArgumentCount, maxArgumentCount);
    }

    protected static String getExpression(String name, List<Roll> arguments) {
        return "%s(%s)".formatted(name, arguments.stream().map(Roll::getExpression).collect(Collectors.joining(",")));
    }
}

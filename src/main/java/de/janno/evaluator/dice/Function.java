package de.janno.evaluator.dice;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A <a href="http://en.wikipedia.org/wiki/Function_(mathematics)">function</a>.
 */

@Getter
@EqualsAndHashCode
@ToString
public abstract class Function {
    @NonNull
    private final ImmutableSet<String> names;
    private final int minArgumentCount;
    private final int maxArgumentCount;

    /**
     * This constructor builds a function with a fixed arguments count.
     *
     * @param name          The function's name
     * @param argumentCount The function's argument count.
     * @throws IllegalArgumentException if argumentCount is lower than 0 or if the function name is null or empty.
     */
    public Function(@NonNull String name, int argumentCount) {
        this(ImmutableSet.of(name), argumentCount, argumentCount);
    }

    public Function(@NonNull String name, int minArgumentCount, int maxArgumentCount) {
        this(ImmutableSet.of(name), minArgumentCount, maxArgumentCount);
    }

    /**
     * This constructor builds a function with a variable arguments count.
     *
     * @param names            The function's names
     * @param minArgumentCount The function's minimum argument count.
     * @param maxArgumentCount The function's maximum argument count (Integer.MAX_VALUE to specify no upper limit).
     * @throws IllegalArgumentException if minArgumentCount is less than 0 or greater than maxArgumentCount or if the function name is null or empty.
     */
    public Function(@NonNull Set<String> names, int minArgumentCount, int maxArgumentCount) {
        if ((minArgumentCount < 0) || (minArgumentCount > maxArgumentCount)) {
            throw new IllegalArgumentException("Invalid argument count");
        }
        if (names.size() == 0) {
            throw new IllegalArgumentException("Function names can't be empty");
        }
        if (names.stream().anyMatch(Strings::isNullOrEmpty)) {
            throw new IllegalArgumentException("Function name can't be null or empty");
        }
        this.names = ImmutableSet.copyOf(names);
        this.minArgumentCount = minArgumentCount;
        this.maxArgumentCount = maxArgumentCount;
    }

    protected static String getExpression(String name, List<Roll> arguments) {
        return "%s(%s)".formatted(name, arguments.stream().map(Roll::getExpression).collect(Collectors.joining(",")));
    }

    public String getName() {
        if (names.size() == 1) {
            return names.iterator().next();
        }
        return names.toString();
    }

    public @NonNull String getPrimaryName() {
        return names.iterator().next();
    }

    public abstract @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException;
}
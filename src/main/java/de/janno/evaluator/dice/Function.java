package de.janno.evaluator.dice;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A <a href="http://en.wikipedia.org/wiki/Function_(mathematics)">function</a>.
 */

@Getter
@EqualsAndHashCode
@ToString
public abstract class Function {
    @NonNull
    private final String name;
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
        this(name, argumentCount, argumentCount);
    }

    /**
     * This constructor builds a function with a variable arguments count.
     *
     * @param name             The function's names
     * @param minArgumentCount The function's minimum argument count.
     * @param maxArgumentCount The function's maximum argument count (Integer.MAX_VALUE to specify no upper limit).
     * @throws IllegalArgumentException if minArgumentCount is less than 0 or greater than maxArgumentCount or if the function name is null or empty.
     */
    public Function(@NonNull String name, int minArgumentCount, int maxArgumentCount) {
        if ((minArgumentCount < 0) || (minArgumentCount > maxArgumentCount)) {
            throw new IllegalArgumentException("Invalid argument count");
        }
        this.name = name;
        this.minArgumentCount = minArgumentCount;
        this.maxArgumentCount = maxArgumentCount;
    }


    protected static String getExpression(String name, List<RollBuilder> arguments) {
        return "%s%s".formatted(name, arguments.stream().map(RollBuilder::toExpression).collect(Collectors.joining(",")));
    }

    /**
     * Creates a RollBuilder for the arguments
     * @param arguments all function arguments
     * @param inputValue the given input for the function name, is needed the get the used upper/lower case in the result expression
     * @return the RollBuilder that can be called to get result rolls
     */
    public abstract @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException;
}
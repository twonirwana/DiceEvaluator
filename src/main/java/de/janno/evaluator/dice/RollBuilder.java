package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface RollBuilder {
    static List<Roll> extendAllBuilder(List<RollBuilder> rollBuilders, Map<String, Roll> variableMap) throws ExpressionException {
        ImmutableList.Builder<Roll> builder = ImmutableList.builder();
        for (RollBuilder rs : rollBuilders) {
            extendRollBuilder(variableMap, rs, builder);
        }
        return builder.build();
    }

    static void extendRollBuilder(@NonNull Map<String, Roll> variableMap, RollBuilder rs, ImmutableList.Builder<Roll> builder) throws ExpressionException {
        Optional<List<Roll>> r = rs.extendRoll(variableMap);
        //don't add empty rolls, they are the result from the value function and change the number of rolls (and then the operator/function
        //uses the roll with the wrong index
        r.ifPresent(builder::addAll);
    }

    /**
     * Creates a concrete roll from a roll builder (applies all random function aka throwing the dice).
     * <p>
     * Some functions or operators (e.g. val repeatList) produces empty results, they musst be filtered out or the argument count in functions are not correct.
     * This is not a problem in operators because there the number of arguments is always correct because val is already pushed on the result stack
     */
    @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variableMap) throws ExpressionException;

}

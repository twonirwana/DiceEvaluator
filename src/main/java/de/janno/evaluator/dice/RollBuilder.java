package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        List<Roll> r = rs.extendRoll(variableMap);
        //don't add empty rolls, they are the result from the value function and change the number of rolls (and then the operator/function
        //uses the roll with the wrong index
        if (!r.isEmpty()) {
            builder.addAll(r);
        }
    }

    static RollsAndIndex getFirstNonEmptyRolls(List<RollBuilder> rollBuilders, Map<String, Roll> variableMap) throws ExpressionException {
        List<Roll> firstNonEmptyRoll = Collections.emptyList();
        int index = 0;
        //todo all are empty
        while (firstNonEmptyRoll.isEmpty()) {
            firstNonEmptyRoll = rollBuilders.get(index).extendRoll(variableMap);
            index++;
        }
        return new RollsAndIndex(firstNonEmptyRoll, index - 1);
    }

    /**
     * val produces empty results, they musst be filtered out or the argument count in functions are not correct.
     * This is not a problem in operators because there the number of arguments is always correct because val is already pushed on the result stack
     */
    //todo return optional
    @NonNull List<Roll> extendRoll(@NonNull Map<String, Roll> variableMap) throws ExpressionException;

    @Value
    class RollsAndIndex {
        @NonNull List<Roll> rolls;
        int index;
    }
}

package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface RollBuilder {
    static List<Roll> extendAllBuilder(List<RollBuilder> rollBuilders, Map<String, Roll> constantMap) throws ExpressionException {
        ImmutableList.Builder<Roll> builder = ImmutableList.builder();
        for (RollBuilder rs : rollBuilders) {
            List<Roll> r = rs.extendRoll(constantMap);
            builder.addAll(r);
        }
        return builder.build();
    }

    @NonNull List<Roll> extendRoll(@NonNull Map<String, Roll> constantMap) throws ExpressionException;
}

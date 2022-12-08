package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public final class EvaluationUtils {
    public static List<Roll> rollAllSupplier(List<RollSupplier> rollSuppliers, Map<String, Roll> constantMap) throws ExpressionException {
        ImmutableList.Builder<Roll> builder = ImmutableList.builder();
        for (RollSupplier rs : rollSuppliers) {
            Roll r = rs.roll(constantMap);
            builder.add(r);
        }
        return builder.build();
    }
}

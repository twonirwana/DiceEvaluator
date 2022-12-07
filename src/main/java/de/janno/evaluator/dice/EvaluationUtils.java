package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class EvaluationUtils {
    public static List<Roll> rollAllSupplier(List<RollSupplier> rollSuppliers) throws ExpressionException {
        ImmutableList.Builder<Roll> builder = ImmutableList.builder();
        for (RollSupplier rs : rollSuppliers) {
            builder.add(rs.roll());
        }
        return builder.build();
    }
}

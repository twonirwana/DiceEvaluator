package de.janno.evaluator.dice;

import lombok.NonNull;

import java.util.Map;

@FunctionalInterface
public interface RollSupplier {
    @NonNull Roll roll(@NonNull Map<String, Roll> constantMap) throws ExpressionException;

}

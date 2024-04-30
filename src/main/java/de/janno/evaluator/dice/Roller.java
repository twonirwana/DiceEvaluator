package de.janno.evaluator.dice;

import lombok.NonNull;

import java.util.List;

@FunctionalInterface
public interface Roller {
    @NonNull RollResult roll() throws ExpressionException;
}

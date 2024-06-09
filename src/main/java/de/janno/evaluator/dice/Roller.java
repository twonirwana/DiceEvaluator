package de.janno.evaluator.dice;

import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

public interface Roller {

    /**
     * Rolls the roller with the default number supplier
     */
    @NonNull
    RollResult roll() throws ExpressionException;

    /**
     * Rolls the roller with the given number supplier
     */
    @NonNull
    RollResult roll(NumberSupplier numberSupplier) throws ExpressionException;
}

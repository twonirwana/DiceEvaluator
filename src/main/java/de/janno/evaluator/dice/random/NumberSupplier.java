package de.janno.evaluator.dice.random;

import de.janno.evaluator.dice.DieId;
import de.janno.evaluator.dice.ExpressionException;
import lombok.NonNull;

import javax.annotation.Nullable;

/**
 * Provides a number between minExcl and maxIncl.
 * Normally this is a random number, but for testing it can be implemented to provide specific numbers.
 */
public interface NumberSupplier {

    /**
     * returns a number between minExcl and maxIncl
     *
     * @param minExcl the minimum value (exclusive)
     * @param maxIncl the maximum value (inclusive)
     * @param dieId the id of the die in the expression that is rolled
     * @return a number between minExcl and maxIncl
     */
    int get(int minExcl, int maxIncl, @Nullable DieId dieId) throws ExpressionException;
}

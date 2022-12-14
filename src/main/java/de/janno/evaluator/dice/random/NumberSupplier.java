package de.janno.evaluator.dice.random;

import de.janno.evaluator.dice.ExpressionException;

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
     * @return a number between minExcl and maxIncl
     */
    int get(int minExcl, int maxIncl) throws ExpressionException;
}

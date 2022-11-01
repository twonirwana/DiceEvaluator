package de.janno.evaluator.dice.random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * Provides random numbers
 */
public class RandomNumberSupplier implements NumberSupplier {
    private final Sfc64Random randomNumberGenerator;

    public RandomNumberSupplier() {
        randomNumberGenerator = new Sfc64Random();
    }

    @VisibleForTesting
    RandomNumberSupplier(long seed) {
        randomNumberGenerator = new Sfc64Random(seed);
    }

    @Override
    public int get(int minExcl, int maxIncl) {
        Preconditions.checkArgument(minExcl < maxIncl, "minExcl must be smaller than maxIncl");
        int diff = maxIncl - minExcl + 1;
        return (int) (minExcl + (randomNumberGenerator.nextDouble() * diff));
    }
}

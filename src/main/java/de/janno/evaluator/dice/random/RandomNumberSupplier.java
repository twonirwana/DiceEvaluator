package de.janno.evaluator.dice.random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * Provides random numbers
 */
public class RandomNumberSupplier implements NumberSupplier {
    private final ThreadLocalSfc64Random randomSource;

    public RandomNumberSupplier() {
        randomSource = new ThreadLocalSfc64Random();
    }

    @VisibleForTesting
    RandomNumberSupplier(long seed) {
        randomSource = new ThreadLocalSfc64Random(seed);
    }

    public int get(int minExcl, int maxIncl) {
        Preconditions.checkArgument(minExcl < maxIncl, "minExcl must be smaller than maxIncl");
        Preconditions.checkArgument(maxIncl < Integer.MAX_VALUE, "maxIncl must be smaller than " + Integer.MAX_VALUE);
        if (minExcl + 1 == maxIncl) {
            return maxIncl;
        }
        return randomSource.nextInt(minExcl + 1, maxIncl + 1);
    }
}

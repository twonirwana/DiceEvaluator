package de.janno.evaluator.dice.random;

import com.google.common.annotations.VisibleForTesting;
import de.janno.evaluator.dice.DieId;
import de.janno.evaluator.dice.ExpressionException;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.random.RandomGenerator;

/**
 * Provides random numbers
 */
public class RandomNumberSupplier implements NumberSupplier {
    private final RandomGenerator randomSource;

    public RandomNumberSupplier() {
        randomSource = new ThreadLocalSfc64Random();
    }

    @VisibleForTesting
    public RandomNumberSupplier(long seed) {
        randomSource = new Sfc64Random(seed);
    }

    public int get(int minExcl, int maxIncl, @Nullable DieId dieId) throws ExpressionException {
        if (minExcl == Integer.MAX_VALUE) {
            throw new ExpressionException("Cannot give a random number for minExcl =%d".formatted(Integer.MAX_VALUE));
        }
        if (maxIncl == Integer.MAX_VALUE) {
            throw new ExpressionException("Cannot give a random number for maxIncl =%d".formatted(Integer.MAX_VALUE));
        }
        if (minExcl >= maxIncl) {
            throw new ExpressionException("Random number between %d (excl) and %d (incl) is not possible".formatted(minExcl, maxIncl));
        }
        if (minExcl + 1 == maxIncl) {
            return maxIncl;
        }
        return randomSource.nextInt(minExcl + 1, maxIncl + 1);
    }
}

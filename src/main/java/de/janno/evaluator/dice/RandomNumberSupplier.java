package de.janno.evaluator.dice;

import java.util.Random;

public class RandomNumberSupplier implements NumberSupplier {
    private static final Random randomNumberGenerator = new Random();

    @Override
    public int get(int minExcl, int maxIncl) {
        return (int) (minExcl + 1 + (randomNumberGenerator.nextDouble() * maxIncl));
    }
}

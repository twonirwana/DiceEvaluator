package de.janno.evaluator.dice.random;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

/**
 * An implementation of the SFC64 random number generator by Chris Doty-Humphrey.
 * Based on the implementation by M. E. O'Neill (<a href="https://gist.github.com/imneme/f1f7821f07cf76504a97f6537c818083">...</a>)
 * who in turn derived it from Chris Doty-Humphrey's PractRand (<a href="https://pracrand.sourceforge.net/">...</a>)
 * Not thread-safe, use ThreadLocalSfc64Random for that.
 */
public class Sfc64Random implements RandomGenerator {
    private long state_a;
    private long state_b;
    private long state_c;
    private long counter;

    public Sfc64Random(long seed, long seed2, long seed3) {
        doSeed(seed, seed2, seed3);
    }

    public Sfc64Random(long seed, long seed2) {
        doSeed(seed, seed2);
    }

    public Sfc64Random(long seed) {
        doSeed(seed);
    }

    public Sfc64Random() {
        final SecureRandom seed_source = new SecureRandom();

        doSeed(seed_source.nextLong(), seed_source.nextLong(), seed_source.nextLong());
    }

    public long nextLong() {
        final long result = state_a + state_b + counter++;

        state_a = state_b ^ (state_b >>> 11);
        state_b = state_c + (state_c << 3);
        state_c = result + Long.rotateLeft(state_c, 24);

        return result;
    }

    private void doSeed(long seed_c, long seed_b, long seed_a) {
        state_a = seed_a;
        state_b = seed_b;
        state_c = seed_c;
        counter = 1;

        // Scramble the state a little.
        for (int i = 0; i < 20; i++) {
            nextLong();
        }
    }

    // Both of these simply add seed extender values. For cases when there are less
    // than 3 longs of seed data, PractRand encourages the order C, B, A.
    private void doSeed(long seed_c, long seed_b) {
        doSeed(seed_c, seed_b, 3141592653589793L);
    }

    private void doSeed(long seed) {
        doSeed(seed, 0xDABEEF5EEDC00L);
    }
}
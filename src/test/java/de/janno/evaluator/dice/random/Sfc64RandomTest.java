package de.janno.evaluator.dice.random;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sfc64RandomTest {

    @Test
    void testSeed() {
        Sfc64Random underTest = new Sfc64Random(0);
        assertThat(underTest.nextLong()).isEqualTo(5379876859184494722L);
    }
}
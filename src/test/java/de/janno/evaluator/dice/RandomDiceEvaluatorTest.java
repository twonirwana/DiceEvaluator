package de.janno.evaluator.dice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomDiceEvaluatorTest {
    @Test
    void testWithRandom() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator();

        List<Roll> res = underTest.evaluate("1d6");

        assertThat(res.size()).isEqualTo(1);
        assertThat(res.getFirst().getElements().size()).isEqualTo(1);
        assertThat(res.getFirst().getElements().getFirst().asInteger().orElseThrow()).isGreaterThanOrEqualTo(1);
        assertThat(res.getFirst().getElements().getFirst().asInteger().orElseThrow()).isLessThanOrEqualTo(6);
    }

}

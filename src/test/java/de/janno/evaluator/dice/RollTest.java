package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RollTest {

    @Test
    void testVerification() {
        DieId dieId = DieId.of(2, "d", 0, 0, 0);
        assertThatThrownBy(() -> new Roll("2d6", ImmutableList.of(), ImmutableList.of(
                ImmutableList.of(new RandomElement(new RollElement("1", RollElement.NO_COLOR, RollElement.NO_TAG), 0, 6, dieId, 1)),
                ImmutableList.of(new RandomElement(new RollElement("1", RollElement.NO_COLOR, RollElement.NO_TAG), 0, 6, dieId, 1))),
                ImmutableList.of(),
                ExpressionPosition.of(2, "d"), 100, false))
                .isInstanceOfAny(IllegalStateException.class)
                .hasMessage("Random elements must have unique dice ids but [2de0i0r0] occurred more than once");
    }
}

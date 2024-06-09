package de.janno.evaluator.dice.random;

import de.janno.evaluator.dice.DiceIdAndValue;
import de.janno.evaluator.dice.DieId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GivenDiceNumberSupplierTest {

    @Test
    void setDuplicateId() {
        assertThatThrownBy(() -> new GivenDiceNumberSupplier(List.of(
                DiceIdAndValue.of(DieId.of(1, "d", 1, 1, 1), 1),
                DiceIdAndValue.of(DieId.of(1, "d", 1, 1, 1), 2)
        )))
                .isInstanceOfAny(IllegalStateException.class)
                .hasMessage("Duplicated dice ids: [1de1i1r1]");
    }
}
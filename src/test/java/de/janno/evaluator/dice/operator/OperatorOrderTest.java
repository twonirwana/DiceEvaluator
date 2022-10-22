package de.janno.evaluator.dice.operator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperatorOrderTest {

    @Test
    void uniqueInsertTest() {
        assertThat(OperatorOrder.operatorOrderList.size())
                .isEqualTo(OperatorOrder.operatorOrderList.stream().distinct().count());
    }
}

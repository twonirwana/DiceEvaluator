package de.janno.evaluator.dice.random;

import de.janno.evaluator.ExpressionException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class RandomNumberSupplierTest {


    @Test
    void d6Test() throws ExpressionException {
        int testSize = 1_000_000;
        RandomNumberSupplier underTest = new RandomNumberSupplier(0);
        List<Integer> res = new ArrayList<>(testSize);

        for (int i = 0; i < testSize; i++) {
            res.add(underTest.get(0, 6));
        }

        assertThat(res).allMatch(i -> i >= 1 && i <= 6);
        assertThat(res).containsOnly(1, 2, 3, 4, 5, 6);
        assertThat(res.stream().mapToInt(i -> i).average().orElseThrow()).isEqualTo(3.5, offset(0.01));
    }

    @Test
    void from10to100Test() throws ExpressionException {
        int testSize = 1_000_000;
        RandomNumberSupplier underTest = new RandomNumberSupplier(0);
        List<Integer> res = new ArrayList<>(testSize);

        for (int i = 0; i < testSize; i++) {
            res.add(underTest.get(9, 100));
        }

        assertThat(res).allMatch(i -> i >= 10 && i <= 100);
        assertThat(res).contains(10, 100);
        assertThat(res.stream().mapToInt(i -> i).average().orElseThrow()).isEqualTo(55, offset(0.05));
    }

    @Test
    void negativPositivTest() throws ExpressionException {
        int testSize = 1_000_000;
        RandomNumberSupplier underTest = new RandomNumberSupplier(0);
        List<Integer> res = new ArrayList<>(testSize);

        for (int i = 0; i < testSize; i++) {
            res.add(underTest.get(-4, 3));
        }

        assertThat(res).allMatch(i -> i >= -3 && i <= 3);
        assertThat(res).containsOnly(-3, -2, -1, 0, 1, 2, 3);
        assertThat(res.stream().mapToInt(i -> i).average().orElseThrow()).isEqualTo(0, offset(0.01));
    }

    @Test
    void negativTest() throws ExpressionException {
        int testSize = 1_000_000;
        RandomNumberSupplier underTest = new RandomNumberSupplier(0);
        List<Integer> res = new ArrayList<>(testSize);

        for (int i = 0; i < testSize; i++) {
            res.add(underTest.get(-7, -1));
        }

        assertThat(res).allMatch(i -> i >= -6 && i <= -1);
        assertThat(res).containsOnly(-1, -2, -3, -4, -5, -6);
        assertThat(res.stream().mapToInt(i -> i).average().orElseThrow()).isEqualTo(-3.5, offset(0.01));
    }
}
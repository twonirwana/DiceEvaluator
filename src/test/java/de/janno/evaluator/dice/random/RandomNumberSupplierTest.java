package de.janno.evaluator.dice.random;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class RandomNumberSupplierTest {

    RandomNumberSupplier underTest = new RandomNumberSupplier(0);

    @Test
    void d6Test() {
        List<Integer> res = IntStream.range(0, 1_000_000)
                .map(i -> underTest.get(0, 6))
                .boxed()
                .toList();

        assertThat(res).allMatch(i -> i >= 1 && i <= 6);
        assertThat(res).contains(1, 2, 3, 4, 5, 6);
        assertThat(res.stream().mapToInt(i -> i).average().orElseThrow()).isEqualTo(3.5, offset(0.01));
    }

    @Test
    void from10to100Test() {
        List<Integer> res = IntStream.range(0, 1_000_000)
                .map(i -> underTest.get(9, 100))
                .boxed()
                .toList();

        assertThat(res).allMatch(i -> i >= 10 && i <= 100);
        assertThat(res).contains(10, 100);
        assertThat(res.stream().mapToInt(i -> i).average().orElseThrow()).isEqualTo(55, offset(0.05));
    }
}
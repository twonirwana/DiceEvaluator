package de.janno.evaluator.dice.random;

import com.google.common.annotations.VisibleForTesting;

import java.util.*;

@VisibleForTesting
public class GivenNumberSupplier implements NumberSupplier {

    final private Deque<Integer> results;

    public GivenNumberSupplier() {
        this(Collections.emptyList());
    }

    public GivenNumberSupplier(Integer... results) {
        this(Arrays.asList(results));
    }

    public GivenNumberSupplier(Collection<Integer> results) {
        if (results == null) {
            this.results = new ArrayDeque<>();
        } else {
            this.results = new ArrayDeque<>(results);
        }
    }

    @Override
    public int get(int minExcl, int maxIncl) {
        if (results.isEmpty()) {
            return maxIncl;
        }
        return results.pop();
    }
}

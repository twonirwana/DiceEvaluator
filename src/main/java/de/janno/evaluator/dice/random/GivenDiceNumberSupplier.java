package de.janno.evaluator.dice.random;

import com.google.common.annotations.VisibleForTesting;
import de.janno.evaluator.dice.DieId;
import de.janno.evaluator.dice.ExpressionException;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GivenDiceNumberSupplier implements NumberSupplier {
    private final NumberSupplier numberSupplier;
    private final Map<DieId, Integer> givenDiceNumbers;

    public GivenDiceNumberSupplier(@NonNull Map<DieId, Integer> givenDiceNumbers) {
        numberSupplier = new RandomNumberSupplier();
        this.givenDiceNumbers = new ConcurrentHashMap<>(givenDiceNumbers);
    }

    @VisibleForTesting
    public GivenDiceNumberSupplier(@NonNull NumberSupplier numberSupplier, @NonNull Map<DieId, Integer> givenDiceNumbers) {
        this.numberSupplier = numberSupplier;
        this.givenDiceNumbers = new ConcurrentHashMap<>(givenDiceNumbers);
    }

    @Override
    public int get(int minExcl, int maxIncl, @NonNull DieId dieId) throws ExpressionException {
        if (givenDiceNumbers.containsKey(dieId)) {
            return givenDiceNumbers.remove(dieId);
        }

        return numberSupplier.get(minExcl, maxIncl, dieId);
    }

    public boolean allStoredDiceUsed() {
        return givenDiceNumbers.isEmpty();
    }
}

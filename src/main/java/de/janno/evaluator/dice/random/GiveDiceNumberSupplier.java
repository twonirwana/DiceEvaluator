package de.janno.evaluator.dice.random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import de.janno.evaluator.dice.DieId;
import de.janno.evaluator.dice.ExpressionException;
import lombok.NonNull;

public class GiveDiceNumberSupplier implements NumberSupplier {
    private final NumberSupplier numberSupplier;
    private final ImmutableMap<DieId, Integer> givenDiceNumbers;

    public GiveDiceNumberSupplier(@NonNull ImmutableMap<DieId, Integer> givenDiceNumbers) {
        numberSupplier = new RandomNumberSupplier();
        this.givenDiceNumbers = givenDiceNumbers;
    }

    @VisibleForTesting
    public GiveDiceNumberSupplier(@NonNull NumberSupplier numberSupplier, @NonNull ImmutableMap<DieId, Integer> givenDiceNumbers) {
        this.numberSupplier = numberSupplier;
        this.givenDiceNumbers = givenDiceNumbers;
    }

    @Override
    public int get(int minExcl, int maxIncl, DieId dieId) throws ExpressionException {
        if (givenDiceNumbers.get(dieId) != null) {
            //todo
            return givenDiceNumbers.get(dieId);
        }
        return numberSupplier.get(minExcl, maxIncl, dieId);
    }
}

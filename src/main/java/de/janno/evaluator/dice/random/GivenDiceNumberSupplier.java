package de.janno.evaluator.dice.random;

import com.google.common.annotations.VisibleForTesting;
import de.janno.evaluator.dice.DiceIdAndValue;
import de.janno.evaluator.dice.DieId;
import de.janno.evaluator.dice.ExpressionException;
import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GivenDiceNumberSupplier implements NumberSupplier {
    private final NumberSupplier numberSupplier;
    private final Map<DieId, Integer> givenDiceNumbers;

    public GivenDiceNumberSupplier(@NonNull List<DiceIdAndValue> givenDiceNumbers) {
        this(new RandomNumberSupplier(), givenDiceNumbers);
    }

    @VisibleForTesting
    public GivenDiceNumberSupplier(@NonNull NumberSupplier numberSupplier, @NonNull List<DiceIdAndValue> givenDiceNumbers) {
        this.numberSupplier = numberSupplier;
        Set<DieId> allDiceIds = new HashSet<>();
        Set<DieId> duplicatedDiceIds = new HashSet<>();
        for (DiceIdAndValue diceIdAndValue : givenDiceNumbers) {
            if (allDiceIds.contains(diceIdAndValue.getDieId())) {
                duplicatedDiceIds.add(diceIdAndValue.getDieId());
            }
            allDiceIds.add(diceIdAndValue.getDieId());
        }
        if (!duplicatedDiceIds.isEmpty()) {
            throw new IllegalStateException("Duplicated dice ids: " + duplicatedDiceIds);
        }
        this.givenDiceNumbers = givenDiceNumbers.stream().collect(Collectors.toMap(DiceIdAndValue::getDieId, DiceIdAndValue::getNumberSupplierValue));
    }

    @Override
    public int get(int minExcl, int maxIncl, @NonNull DieId dieId) throws ExpressionException {
        if (givenDiceNumbers.containsKey(dieId)) {
            return givenDiceNumbers.get(dieId);
        }

        return numberSupplier.get(minExcl, maxIncl, dieId);
    }

}

package de.janno.evaluator.dice;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class DiceIdAndValue {
    @NonNull
    DieId dieId;

    int numberSupplierValue;

}

package de.janno.evaluator.dice;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class DieIdAndValue {
    @NonNull
    DieId dieId;

    int numberSupplierValue;

}

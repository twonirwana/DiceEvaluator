package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;


@Value
public class RandomElement {

    @NonNull
    RollElement rollElement;

    @Nullable
    ImmutableList<String> randomSelectedFrom;

    int numberSupplierValue;

    @NonNull
    DieId dieId;

    @Nullable
    Integer minInc;

    @Nullable
    Integer maxInc;

    public RandomElement(@NonNull RollElement rollElement, @NonNull ImmutableList<String> randomSelectedFrom, @NonNull DieId dieId, int numberSupplierValue) {
        this(rollElement, randomSelectedFrom, null, null, dieId, numberSupplierValue);
    }

    public RandomElement(@NonNull RollElement rollElement, int minInc, int maxInc, @NonNull DieId dieId, int numberSupplierValue) {
        this(rollElement, null, minInc, maxInc, dieId, numberSupplierValue);
    }

    private RandomElement(@NonNull RollElement rollElement,
                          @Nullable ImmutableList<String> randomSelectedFrom,
                          @Nullable Integer minInc,
                          @Nullable Integer maxInc,
                          @NonNull DieId dieId,
                          int numberSupplierValue) {
        this.rollElement = rollElement;
        this.randomSelectedFrom = randomSelectedFrom;
        this.numberSupplierValue = numberSupplierValue;
        this.minInc = minInc;
        this.maxInc = maxInc;
        this.dieId = dieId;
    }

    public RandomElement copyWithTagAndColor(@NonNull String color) {
        return new RandomElement(new RollElement(this.rollElement.getValue(), this.rollElement.getTag(), color), this.randomSelectedFrom, this.minInc, this.maxInc, this.dieId, this.numberSupplierValue);
    }

    public String toString() {
        if (randomSelectedFrom != null) {
            return "%s=%s∈%s".formatted(dieId, rollElement.toStringWithColorAndTag(), randomSelectedFrom);
        } else {
            return "%s=%s∈[%d...%d]".formatted(dieId, rollElement.toStringWithColorAndTag(), minInc, maxInc);
        }
    }

    public DieIdAndValue getDiceIdAndValue() {
        return DieIdAndValue.of(dieId, numberSupplierValue);
    }
}

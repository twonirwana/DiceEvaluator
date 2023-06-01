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

    @Nullable
    Integer minInc;

    @Nullable
    Integer maxInc;

    public RandomElement(@NonNull RollElement rollElement, @NonNull ImmutableList<String> randomSelectedFrom) {
        this(rollElement, randomSelectedFrom, null, null);

    }

    public RandomElement(@NonNull RollElement rollElement, int minInc, int maxInc) {
        this(rollElement, null, minInc, maxInc);
    }

    private RandomElement(@NonNull RollElement rollElement,
                          @Nullable ImmutableList<String> randomSelectedFrom,
                          @Nullable Integer minInc,
                          @Nullable Integer maxInc) {
        this.rollElement = rollElement;
        this.randomSelectedFrom = randomSelectedFrom;
        this.minInc = minInc;
        this.maxInc = maxInc;
    }

    public RandomElement copyWithTagAndColor(@NonNull String color) {
        return new RandomElement(new RollElement(this.rollElement.getValue(), this.rollElement.getTag(), color), this.randomSelectedFrom, this.minInc, this.maxInc);
    }

    public String toString() {
        if (randomSelectedFrom != null) {
            return "%s∈%s".formatted(rollElement.toString(), randomSelectedFrom);
        } else {
            return "%s∈[%d...%d]".formatted(rollElement.toString(), minInc, maxInc);
        }
    }
}

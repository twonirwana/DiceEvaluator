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

    public RandomElement(@NonNull RollElement rollElement, ImmutableList<String> randomSelectedFrom) {
        this.rollElement = rollElement;
        this.randomSelectedFrom = randomSelectedFrom;
        this.minInc = null;
        this.maxInc = null;
    }

    public RandomElement(@NonNull RollElement rollElement, int minInc, int maxInc) {
        this.rollElement = rollElement;
        this.minInc = minInc;
        this.maxInc = maxInc;
        this.randomSelectedFrom = null;
    }

    public String toString() {
        if (randomSelectedFrom != null) {
            return "%s∈%s".formatted(rollElement.getValue(), randomSelectedFrom);
        } else {
            return "%s∈[%d...%d]".formatted(rollElement.getValue(), minInc, maxInc);
        }
    }
}

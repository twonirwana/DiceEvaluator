package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
public class RandomElement implements Comparable<RandomElement> {
    @NonNull
    String value;

    @Nullable
    ImmutableList<String> randomSelectedFrom;

    @Nullable
    Integer minInc;

    @Nullable
    Integer maxInc;

    public RandomElement(@NonNull String value, ImmutableList<String> randomSelectedFrom) {
        this.value = value;
        this.randomSelectedFrom = randomSelectedFrom;
        this.minInc = null;
        this.maxInc = null;
    }

    public RandomElement(@NonNull String value, int minInc, int maxInc) {
        this.value = value;
        this.minInc = minInc;
        this.maxInc = maxInc;
        this.randomSelectedFrom = null;
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String toString() {
        if (randomSelectedFrom != null) {
            return "%s∈%s".formatted(value, randomSelectedFrom);
        } else {
            return "%s∈[%d...%d]".formatted(value, minInc, maxInc);
        }
    }

    public Optional<Integer> asInteger() {
        if (isInteger(value)) {
            return Optional.of(Integer.parseInt(value));
        }
        return Optional.empty();
    }

    @Override
    public int compareTo(@NonNull RandomElement rollElement) {
        if (asInteger().isPresent() && rollElement.asInteger().isPresent()) {
            return asInteger().get().compareTo(rollElement.asInteger().get());
        }
        return this.getValue().compareTo(rollElement.getValue());
    }
}

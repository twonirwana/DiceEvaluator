package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Optional;

@Value
@RequiredArgsConstructor
public class RandomElement implements Comparable<RandomElement> {
    @NonNull
    String value;

    @NonNull
    ImmutableList<String> randomSelectedFrom;

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
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

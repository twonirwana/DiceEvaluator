package de.janno.evaluator.dice;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Optional;

@Value
@RequiredArgsConstructor
public class ResultElement implements Comparable<ResultElement> {
    public static final String NO_COLOR = "";
    @NonNull
    String value;
    @NonNull
    String color;

    public Optional<Integer> asInteger() {
        if (NumberUtils.isParsable(value)) {
            return Optional.of(Integer.parseInt(value));
        }
        return Optional.empty();
    }

    @Override
    public int compareTo(@NonNull ResultElement resultElement) {
        if (!this.getColor().equals(resultElement.getColor())) {
            return this.getColor().compareTo(resultElement.getColor());
        }
        if (asInteger().isPresent() && resultElement.asInteger().isPresent()) {
            return asInteger().get().compareTo(resultElement.asInteger().get());
        }
        return this.getValue().compareTo(resultElement.getValue());
    }
}

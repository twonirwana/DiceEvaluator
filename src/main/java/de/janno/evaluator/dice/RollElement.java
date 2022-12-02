package de.janno.evaluator.dice;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Optional;

@Value
@RequiredArgsConstructor
public class RollElement implements Comparable<RollElement> {
    public static final String NO_COLOR = "";
    @NonNull
    String value;
    @NonNull
    String color;

    public boolean isInteger() {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Optional<Integer> asInteger() {
        if (isInteger()) {
            return Optional.of(Integer.parseInt(value));
        }
        return Optional.empty();
    }

    @Override
    public int compareTo(@NonNull RollElement rollElement) {
        if (!this.getColor().equals(rollElement.getColor())) {
            return this.getColor().compareTo(rollElement.getColor());
        }
        if (asInteger().isPresent() && rollElement.asInteger().isPresent()) {
            return asInteger().get().compareTo(rollElement.asInteger().get());
        }
        return this.getValue().compareTo(rollElement.getValue());
    }

    @Override
    public String toString() {
        String outputColor = NO_COLOR.equals(color) ? "" : "%s:".formatted(color);
        return "%s%s".formatted(outputColor, value);
    }
}

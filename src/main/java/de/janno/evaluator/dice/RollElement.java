package de.janno.evaluator.dice;

import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@RequiredArgsConstructor
public class RollElement implements Comparable<RollElement> {
    public static final String NO_TAG = "";
    public static final String NO_COLOR = "";
    @NonNull
    String value;
    @NonNull
    String tag;
    @NonNull
    String color;


    public Optional<Integer> asInteger() {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }


    public Optional<BigDecimal> asDecimal() {
        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> asBoolean() {
        if (value.equals(String.valueOf(true)) || value.equals("1")) {
            return Optional.of(true);
        }
        if (value.equals(String.valueOf(false)) || value.equals("0")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    @Override
    //the color is not used for compare
    public int compareTo(@NonNull RollElement rollElement) {
        if (!this.getTag().equals(rollElement.getTag())) {
            return this.getTag().compareTo(rollElement.getTag());
        }
        if (asInteger().isPresent() && rollElement.asInteger().isPresent()) {
            return asInteger().get().compareTo(rollElement.asInteger().get());
        }
        return this.getValue().compareTo(rollElement.getValue());
    }

    public boolean isEqualValueAndTag(@NonNull RollElement rollElement) {
        return Objects.equals(this.getTag(), rollElement.getTag()) &&
                Objects.equals(rollElement.getValue(), this.getValue());
    }

    @Override
    public String toString() {
        String outputTag = NO_TAG.equals(tag) ? "" : "%s:".formatted(tag);
        return "%s%s".formatted(outputTag, value);
    }

    public String toStringWithColorAndTag() {
        String outputTag = NO_TAG.equals(tag) ? "" : "t:%s".formatted(tag);
        String outputColor = NO_COLOR.equals(color) ? "" : "c:%s".formatted(color);
        return Stream.of(value, outputTag, outputColor)
                .filter(s -> !Strings.isNullOrEmpty(s))
                .collect(Collectors.joining("-"));
    }
}

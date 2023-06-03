package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class Roll {

    @NonNull
    String expression;
    @NonNull
    ImmutableList<RollElement> elements;
    @NonNull
    //all random elements that were involved in this roll, this can be more than the elements, because it includes also filtered elements
    UniqueRandomElements randomElementsInRoll;
    @NonNull
    ImmutableList<Roll> childrenRolls;

    public Optional<Integer> asInteger() {
        if (elements.size() == 1) {
            return elements.get(0).asInteger();
        }
        return Optional.empty();
    }

    public Optional<BigDecimal> asDecimal() {
        if (elements.size() == 1) {
            return elements.get(0).asDecimal();
        }
        return Optional.empty();
    }

    public Optional<Boolean> asBoolean() {
        if (elements.size() == 1) {
            return elements.get(0).asBoolean();
        }
        return Optional.empty();
    }

    public boolean containsOnlyDecimals() {
        return elements.stream().map(RollElement::asDecimal).allMatch(Optional::isPresent);
    }

    public String getResultString() {
        return elements.stream().map(RollElement::toString).collect(Collectors.joining(", "));
    }

    public String getRandomElementsString() {
        return randomElementsInRoll.getRandomElements().stream()
                .map(l -> l.getRandomElements().stream().map(RandomElement::getRollElement).map(RollElement::toString).toList().toString())
                .collect(Collectors.joining(" "));
    }

    public boolean equalForValueAndTag(Roll other) {
        if (this.getElements().size() != other.getElements().size()) {
            return false;
        }
        for (int i = 0; i < this.getElements().size(); i++) {
            if (!this.getElements().get(i).isEqualValueAndTag(other.getElements().get(i))) {
                return false;
            }
        }
        return true;
    }
}
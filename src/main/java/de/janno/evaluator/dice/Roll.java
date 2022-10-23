package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import java.util.AbstractCollection;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class Roll {

    @NonNull
    String expression;
    @NonNull
    ImmutableList<RollElement> elements;
    @NonNull
    ImmutableList<ImmutableList<RollElement>> randomElementsInRoll;
    @NonNull
    ImmutableList<Roll> childrenRolls;

    public Optional<Integer> asInteger() {
        if (elements.size() == 1) {
            return elements.get(0).asInteger();
        }
        return Optional.empty();
    }

    public boolean containsOnlyIntegers() {
        return elements.stream().map(RollElement::asInteger).allMatch(Optional::isPresent);
    }

    public String getResultString() {
        return elements.stream().map(RollElement::toString).collect(Collectors.joining(", "));
    }

    public String getRandomElementsString() {
        if (randomElementsInRoll.size() == 1) {
            return randomElementsInRoll.get(0).stream().map(RollElement::toString).collect(Collectors.joining(", "));
        }
        return randomElementsInRoll.stream().map(AbstractCollection::toString).collect(Collectors.joining(" "));
    }
}
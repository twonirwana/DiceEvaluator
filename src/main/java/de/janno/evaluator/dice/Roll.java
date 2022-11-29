package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class Roll {

    @NonNull
    String expression;
    @NonNull
    ImmutableList<RollElement> elements;
    @NonNull
    ImmutableList<ImmutableList<RandomElement>> randomElementsInRoll;
    @NonNull
    ImmutableList<Roll> childrenRolls;

    String constantName;

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
        return randomElementsInRoll.stream()
                .map(l -> l.stream().map(RandomElement::getValue).toList().toString())
                .collect(Collectors.joining(" "));
    }
}
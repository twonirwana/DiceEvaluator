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

    public boolean containsOnlyIntegers() {
        return elements.stream().map(RollElement::asInteger).allMatch(Optional::isPresent);
    }

    public String getResultString() {
        return elements.stream().map(RollElement::toString).collect(Collectors.joining(", "));
    }

    public String getRandomElementsString() {
        return randomElementsInRoll.getRandomElements().stream()
                .map(l -> l.getRandomElements().stream().map(RandomElement::getRollElement).map(RollElement::getValue).toList().toString())
                .collect(Collectors.joining(" "));
    }
}
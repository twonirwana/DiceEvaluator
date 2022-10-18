package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class Result {

    @NonNull
    String expression;
    @NonNull
    ImmutableList<ResultElement> elements;
    @NonNull
    ImmutableList<ImmutableList<ResultElement>> randomElementsProducingTheResult;
    @NonNull
    ImmutableList<Result> childrenResults;

    public Optional<Integer> asInteger() {
        if (elements.size() == 1) {
            return elements.get(0).asInteger();
        }
        return Optional.empty();
    }

    public boolean containsOnlyIntegers() {
        return elements.stream().map(ResultElement::asInteger).allMatch(Optional::isPresent);
    }

    public String getResultString() {
        return elements.stream().map(ResultElement::toString).collect(Collectors.joining(", "));
    }

    public String getRandomElementsString() {
        if (randomElementsProducingTheResult.size() == 1) {
            return randomElementsProducingTheResult.get(0).stream().map(ResultElement::toString).collect(Collectors.joining(", "));
        }
        return randomElementsProducingTheResult.toString();
    }
}
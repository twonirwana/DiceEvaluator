package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;

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

    public String getResultString(){
        return elements.toString();
    }

    public String getRandomElementsString(){
        return randomElementsProducingTheResult.toString();
    }
}
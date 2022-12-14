package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import java.util.UUID;

@Value
public class RandomElements {
    //we need an id because we don't want to add RollRandomElements from values multiple times
    UUID uuid = UUID.randomUUID();
    ImmutableList<RandomElement> randomElements;

    public String toString() {
        return randomElements.stream().map(RandomElement::toString).collect(ImmutableList.toImmutableList()).toString();
    }
}

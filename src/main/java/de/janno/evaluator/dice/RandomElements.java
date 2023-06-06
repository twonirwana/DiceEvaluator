package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import java.util.UUID;

@Value
public class RandomElements {
    //we need an id because we don't want to add RollRandomElements from values multiple times
    UUID uuid;
    ImmutableList<RandomElement> randomElements;

    public RandomElements(ImmutableList<RandomElement> randomElements) {
        this(UUID.randomUUID(), randomElements);
    }

    private RandomElements(UUID uuid, ImmutableList<RandomElement> randomElements) {
        this.uuid = uuid;
        this.randomElements = randomElements;
    }

    public String toString() {
        return randomElements.stream().map(RandomElement::toString).collect(ImmutableList.toImmutableList()).toString();
    }

    public RandomElements copyWithColor(String color) {
        //color should overwrite existing RandomElements with the color and should not add a new on. Therefore, the uuid must be copied.
        return new RandomElements(this.getUuid(), randomElements.stream()
                .map(r -> r.copyWithTagAndColor(color))
                .collect(ImmutableList.toImmutableList()));
    }
}

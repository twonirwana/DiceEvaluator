package de.janno.evaluator.dice;


import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Getter
public class UniqueRandomElements {
    private final ImmutableList<RandomElements> randomElements;

    public UniqueRandomElements(ImmutableList<RandomElements> randomElements) {
        ImmutableList<RandomElements> uniqueList = ImmutableList.of();
        for (RandomElements re : randomElements) {
            uniqueList = addElement(uniqueList, re);
        }
        this.randomElements = uniqueList;
    }

    //the last unique RandomElements remains, not the best option because it depends on the correct order of the list.
    //TODO find a better solution to ensure that the last color application will overwrite the randomElements color
    private static ImmutableList<RandomElements> addElement(ImmutableList<RandomElements> randomElements, RandomElements toAdd) {
        if (toAdd.getRandomElements().isEmpty()) {
            return randomElements;
        }
        //add the new one if there is none with the same uuid
        if (randomElements.stream().noneMatch(re -> toAdd.getUuid().equals(re.getUuid()))) {
            return ImmutableList.<RandomElements>builder()
                    .addAll(randomElements)
                    .add(toAdd)
                    .build();
        }
        //if there is one with the same uuid, it gets replaced (relevant for not changing the color of the random elements)
        return randomElements.stream()
                .map(re -> {
                    if (toAdd.getUuid().equals(re.getUuid())) {
                        return toAdd;
                    }
                    return re;
                })
                .collect(ImmutableList.toImmutableList());

    }

    public static UniqueRandomElements from(Collection<Roll> rolls) {
        UniqueRandomElements.Builder builder = UniqueRandomElements.builder();
        rolls.forEach(r -> builder.add(r.getRandomElementsInRoll()));
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UniqueRandomElements empty() {
        return new UniqueRandomElements(ImmutableList.of());
    }

    public String toString() {
        return randomElements.stream().map(RandomElements::toString).collect(Collectors.joining(", "));
    }

    public static class Builder {
        private final ImmutableList.Builder<RandomElements> randomElements = ImmutableList.builder();

        public Builder add(@NonNull RandomElements randomElements) {
            this.randomElements.add(randomElements);
            return this;
        }

        public Builder addAsRandomElements(@NonNull Collection<RandomElement> randomElements) {
            this.randomElements.add(new RandomElements(ImmutableList.copyOf(randomElements)));
            return this;
        }

        public Builder add(@NonNull UniqueRandomElements randomElements) {
            this.randomElements.addAll(randomElements.getRandomElements());
            return this;
        }

        public Builder addWithColor(@NonNull UniqueRandomElements randomElements, @NonNull String color) {
            randomElements.getRandomElements().forEach(re -> this.add(re.copyWithColor(color)));
            return this;
        }

        public UniqueRandomElements build() {
            return new UniqueRandomElements(randomElements.build());
        }
    }
}

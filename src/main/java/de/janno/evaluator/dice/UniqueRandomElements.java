package de.janno.evaluator.dice;


import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Getter
public class UniqueRandomElements {
    private final ImmutableList<RandomElements> randomElements;

    public UniqueRandomElements(ImmutableList<RandomElements> randomElements) {
        Set<UUID> uuids = new HashSet<>();
        this.randomElements = randomElements.stream()
                .filter(r -> !r.getRandomElements().isEmpty())
                .filter(r -> uuids.add(r.getUuid()))
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

        public Builder add(RandomElements randomElements) {
            this.randomElements.add(randomElements);
            return this;
        }

        public Builder addAsRandomElements(Collection<RandomElement> randomElements) {
            this.randomElements.add(new RandomElements(ImmutableList.copyOf(randomElements)));
            return this;
        }

        public Builder add(UniqueRandomElements randomElements) {
            this.randomElements.addAll(randomElements.getRandomElements());
            return this;
        }

        public UniqueRandomElements build() {
            return new UniqueRandomElements(randomElements.build());
        }
    }
}

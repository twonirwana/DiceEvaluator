package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Roll {

    @NonNull
    String expression;
    @NonNull
    ImmutableList<RollElement> elements;

    /**
     * all random elements that were involved in this roll, this can be more than the elements, because it includes also filtered elements
     * The random elements are grouped by rollId
     */
    @NonNull
    ImmutableList<ImmutableList<RandomElement>> randomElementsInRoll;
    /**
     * all rolls that produced this roll. The collection of the childrenRolls can be disabled and the list is then empty
     */

    @NonNull
    ImmutableList<Roll> childrenRolls;

    public Roll(@NonNull String expression,
                @NonNull ImmutableList<RollElement> elements,
                @NonNull ImmutableList<ImmutableList<RandomElement>> randomElementsInRoll,
                @NonNull ImmutableList<Roll> childrenRolls,
                int maxNumberOfElements,
                boolean keepChildRolls) throws ExpressionException {
        this.expression = expression;
        this.elements = elements;
        validate(randomElementsInRoll);
        this.randomElementsInRoll = randomElementsInRoll;
        this.childrenRolls = keepChildRolls ? childrenRolls : ImmutableList.of();
        if (elements.size() > maxNumberOfElements) {
            throw new ExpressionException("To many elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, elements.size()));
        }
        long numberOfRandomElementsInRoll = randomElementsInRoll.stream().mapToLong(List::size).sum();
        if (numberOfRandomElementsInRoll > maxNumberOfElements) {
            throw new ExpressionException("To many random elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, numberOfRandomElementsInRoll));
        }
    }

    private void validate(ImmutableList<ImmutableList<RandomElement>> randomElements) {
        List<DieId> diceIdsWithDuplicatedRandomElements = randomElements.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(RandomElement::getDieId)).values().stream()
                .filter(l -> l.size() > 1)
                .map(r -> r.getFirst().getDieId())
                .toList();

        if (diceIdsWithDuplicatedRandomElements.size() > 1) {
            throw new IllegalStateException("Random elements must have unique dice ids but %s occurred more than once".formatted(diceIdsWithDuplicatedRandomElements));
        }
    }

    public Optional<Integer> asInteger() {
        if (elements.size() == 1) {
            return elements.getFirst().asInteger();
        }
        return Optional.empty();
    }

    public Optional<BigDecimal> asDecimal() {
        if (elements.size() == 1) {
            return elements.getFirst().asDecimal();
        }
        return Optional.empty();
    }

    public Optional<Boolean> asBoolean() {
        if (elements.size() == 1) {
            return elements.getFirst().asBoolean();
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
        return randomElementsInRoll.stream()
                .map(l -> l.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList())
                .map(List::toString)
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

    public boolean isElementsContainsElementWithValueAndTag(RollElement rollElement) {
        return elements.stream().anyMatch(e -> e.isEqualValueAndTag(rollElement));
    }
}
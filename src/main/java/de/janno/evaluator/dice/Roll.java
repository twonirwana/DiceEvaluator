package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.AbstractCollection;
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
    @NonNull
    //all random elements that were involved in this roll, this can be more than the elements, because it includes also filtered elements
    UniqueRandomElements randomElementsInRoll;
    //all rolls that produced this roll
    @NonNull
    ImmutableList<Roll> childrenRolls;

    public Roll(@NonNull String expression,
                @NonNull ImmutableList<RollElement> elements,
                @NonNull UniqueRandomElements randomElementsInRoll,
                @NonNull ImmutableList<Roll> childrenRolls,
                int maxNumberOfElements,
                boolean keepChildRolls) throws ExpressionException {
        this.expression = expression;
        this.elements = elements;
        this.randomElementsInRoll = randomElementsInRoll;
        this.childrenRolls = keepChildRolls ? childrenRolls : ImmutableList.of();
        if (elements.size() > maxNumberOfElements) {
            throw new ExpressionException("To many elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, elements.size()));
        }
        long numberOfRandomElementsInRoll = randomElementsInRoll.getRandomElements().stream().map(RandomElements::getRandomElements).mapToLong(AbstractCollection::size).sum();
        if (numberOfRandomElementsInRoll > maxNumberOfElements) {
            throw new ExpressionException("To many random elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, numberOfRandomElementsInRoll));
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
        return randomElementsInRoll.getRandomElements().stream()
                .map(l -> l.getRandomElements().stream().map(RandomElement::getRollElement).map(RollElement::toString).toList().toString())
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
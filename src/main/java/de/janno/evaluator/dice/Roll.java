package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Roll {

    @NonNull
    String expression;
    @NonNull
    ImmutableList<RollElement> elements;

    /**
     * all random elements that were involved in this roll, this can be more than the elements, because it includes also filtered elements
     */
    @NonNull
    ImmutableList<RandomElement> randomElementsInRoll;
    /**
     * all rolls that produced this roll. The collection of the childrenRolls can be disabled and the list is then empty
     */

    @NonNull
    ImmutableList<Roll> childrenRolls;
    /**
     * The last expression position that created this roll
     */
    @NonNull
    ExpressionPosition expressionPosition;

    public Roll(@NonNull String expression,
                @NonNull ImmutableList<RollElement> elements,
                @NonNull ImmutableList<RandomElement> randomElementsInRoll,
                @NonNull ImmutableList<Roll> childrenRolls,
                @NonNull ExpressionPosition expressionPosition,
                int maxNumberOfElements,
                boolean keepChildRolls) throws ExpressionException {
        this.expression = expression;
        this.elements = elements;
        validate(randomElementsInRoll);
        this.randomElementsInRoll = randomElementsInRoll;
        this.childrenRolls = keepChildRolls ? childrenRolls : ImmutableList.of();
        this.expressionPosition = expressionPosition;
        if (elements.size() > maxNumberOfElements) {
            throw new ExpressionException("To many elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, elements.size()), expressionPosition);
        }
        long numberOfRandomElementsInRoll = randomElementsInRoll.size();
        if (numberOfRandomElementsInRoll > maxNumberOfElements) {
            throw new ExpressionException("To many random elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, numberOfRandomElementsInRoll), expressionPosition);
        }
    }

    private void validate(ImmutableList<RandomElement> randomElements) {
        List<DieId> diceIdsWithDuplicatedRandomElements = randomElements.stream()
                .collect(Collectors.groupingBy(RandomElement::getDieId)).values().stream()
                .filter(l -> l.size() > 1)
                .map(r -> r.getFirst().getDieId())
                .toList();

        if (!diceIdsWithDuplicatedRandomElements.isEmpty()) {
            throw new IllegalStateException("Random elements must have unique dice ids but %s occurred more than once".formatted(diceIdsWithDuplicatedRandomElements));
        }
    }

    public Optional<String> asSingleValue() {
        if (elements.size() == 1) {
            return Optional.of(elements.getFirst().getValue());
        }
        return Optional.empty();
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

    public String getResultStringWithTagAndColor() {
        return elements.stream().map(RollElement::toStringWithColorAndTag).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "Roll{" +
                "expression='" + expression + '\'' +
                ", randomElementsInRoll=" + randomElementsInRoll +
                ", elements=" + elements +
                '}';
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

    public ImmutableList<ImmutableList<RandomElement>> getGroupedRandomElements() {
        List<RollId> rollIds = randomElementsInRoll.stream()
                .map(RandomElement::getDieId)
                .map(DieId::getRollId)
                .distinct()
                .sorted()
                .toList();

        Map<RollId, List<RandomElement>> rollIdListMap = randomElementsInRoll.stream()
                .collect(Collectors.groupingBy(r -> r.getDieId().getRollId()));

        return rollIds.stream()
                .map(rid -> rollIdListMap.get(rid).stream().sorted(Comparator.comparing(RandomElement::getDieId)).collect(ImmutableList.toImmutableList()))
                .collect(ImmutableList.toImmutableList());
    }
}
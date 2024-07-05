package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class RollResult {
    /**
     * The expression that was the input for the roll.
     */
    @NonNull
    String expression;
    /**
     * The result of the expression roll. This can be multiple values, if the expression can't be reduced to a single value.
     */
    @NonNull
    ImmutableList<Roll> rolls;

    /**
     * all random elements in the total result, this can be more as all the sum of randomElements in the rolls because
     * some function produce empty Rolls, that are removed
     */
    @NonNull
    ImmutableList<RandomElement> allRandomElements;

    public ImmutableList<ImmutableList<RandomElement>> getGroupedRandomElements() {
        List<RollId> rollIds = allRandomElements.stream()
                .map(RandomElement::getDieId)
                .map(DieId::getRollId)
                .distinct()
                .sorted()
                .toList();

        Map<RollId, List<RandomElement>> rollIdListMap = allRandomElements.stream()
                .collect(Collectors.groupingBy(r -> r.getDieId().getRollId()));

        return rollIds.stream()
                .map(rid -> rollIdListMap.get(rid).stream().sorted(Comparator.comparing(RandomElement::getDieId)).collect(ImmutableList.toImmutableList()))
                .collect(ImmutableList.toImmutableList());
    }
}

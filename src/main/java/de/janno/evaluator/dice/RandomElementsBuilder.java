package de.janno.evaluator.dice;


import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Combines random elements of multiple sources. Random elements with the will overwrite elements with the diceId that where added bevor
 */
@EqualsAndHashCode
@Getter
public class RandomElementsBuilder {
    private final List<RandomElement> randomElements = new ArrayList<>();
    private final RollContext rollContext;

    private RandomElementsBuilder(@NonNull RollContext rollContext) {
        this.rollContext = rollContext;
    }

    public static ImmutableList<ImmutableList<RandomElement>> fromRolls(@NonNull Collection<Roll> rolls, @NonNull RollContext rollContext) {
        return ofRolls(rolls, rollContext).build();
    }

    public static RandomElementsBuilder empty(@NonNull RollContext rollContext) {
        return new RandomElementsBuilder(rollContext);
    }

    public static RandomElementsBuilder ofRoll(@NonNull Roll roll, @NonNull RollContext rollContext) {
        return ofRolls(List.of(roll), rollContext);
    }

    public static RandomElementsBuilder ofRolls(@NonNull Collection<Roll> rolls, @NonNull RollContext rollContext) {
        RandomElementsBuilder builder = new RandomElementsBuilder(rollContext);
        rolls.forEach(r -> builder.addRandomElements(r.getRandomElementsInRoll().stream().flatMap(Collection::stream).toList()));
        return builder;
    }

    public RandomElementsBuilder addRandomElements(@NonNull Collection<RandomElement> randomElements) {
        this.randomElements.addAll(randomElements);
        return this;
    }

    public RandomElementsBuilder addRoll(@NonNull Roll roll) {
        this.randomElements.addAll(roll.getRandomElementsInRoll().stream().flatMap(Collection::stream).toList());
        return this;
    }

    public RandomElementsBuilder addWithColor(@NonNull Roll roll, @NonNull String color) {
        this.randomElements.addAll(roll.getRandomElementsInRoll().stream().flatMap(Collection::stream)
                .map(r -> r.copyWithTagAndColor(color))
                .toList());
        return this;
    }

    public ImmutableList<ImmutableList<RandomElement>> build() {

        //This ensures that the last color application will overwrite the randomElements color
        List<RandomElement> uniqueList = new ArrayList<>();
        Map<DieId, Integer> dieIdIndexMap = new HashMap<>();
        for (RandomElement re : randomElements) {
            if (dieIdIndexMap.containsKey(re.getDieId())) {
                int index = dieIdIndexMap.get(re.getDieId());
                uniqueList.set(index, re);
            } else {
                int index = uniqueList.size();
                uniqueList.add(re);
                dieIdIndexMap.put(re.getDieId(), index);
            }
        }

        rollContext.addRandomElements(uniqueList);

        List<RollId> rollIds = uniqueList.stream()
                .map(RandomElement::getDieId)
                .map(DieId::getRollId)
                .distinct()
                .sorted()
                .toList();

        Map<RollId, List<RandomElement>> rollIdListMap = uniqueList.stream()
                .collect(Collectors.groupingBy(r -> r.getDieId().getRollId()));

        return rollIds.stream()
                .map(rid -> rollIdListMap.get(rid).stream().sorted(Comparator.comparing(RandomElement::getDieId)).collect(ImmutableList.toImmutableList()))
                .collect(ImmutableList.toImmutableList());
    }


}

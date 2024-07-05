package de.janno.evaluator.dice;


import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

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

    public static ImmutableList<RandomElement> fromRolls(@NonNull Collection<Roll> rolls, @NonNull RollContext rollContext) {
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
        rolls.forEach(r -> builder.addRandomElements(r.getRandomElementsInRoll().stream().toList()));
        return builder;
    }

    public RandomElementsBuilder addRandomElements(@NonNull Collection<RandomElement> randomElements) {
        this.randomElements.addAll(randomElements);
        return this;
    }

    public RandomElementsBuilder addRoll(@NonNull Roll roll) {
        this.randomElements.addAll(roll.getRandomElementsInRoll().stream().toList());
        return this;
    }

    public RandomElementsBuilder addWithColor(@NonNull Roll roll, @NonNull String color) {
        this.randomElements.addAll(roll.getRandomElementsInRoll().stream()
                .map(r -> r.copyWithTagAndColor(color))
                .toList());
        return this;
    }

    public ImmutableList<RandomElement> build() {

        //This ensures that the last color application will overwrite the randomElements color
        Map<DieId, RandomElement> uniqueMap = new HashMap<>(randomElements.size());
        randomElements.forEach(r -> uniqueMap.put(r.getDieId(), r));
        ImmutableList<RandomElement> result = uniqueMap.values().stream()
                .sorted(Comparator.comparing(RandomElement::getDieId))
                .collect(ImmutableList.toImmutableList());
        rollContext.addRandomElements(result);

        return result;
    }


}

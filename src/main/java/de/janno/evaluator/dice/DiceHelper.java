package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

public final class DiceHelper {
    public static @NonNull ImmutableList<RandomElement> explodingDice(int number, int sides, @NonNull NumberSupplier numberSupplier, @NonNull RollId rollId) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive");
        }
        ImmutableList.Builder<RandomElement> resultBuilder = ImmutableList.builder();
        //todo ggf die reihenfolge ändern, erst alle wüfeln und dann alle nachwürfeln zusammen
        for (int i = 0; i < number; i++) {
            resultBuilder.addAll(rollExplodingDie(sides, numberSupplier, rollId, i));
        }
        return resultBuilder.build();
    }

    private static List<RandomElement> rollExplodingDie(int sides, NumberSupplier numberSupplier, RollId rollId, int index) throws ExpressionException {
        ImmutableList.Builder<RandomElement> resultBuilder = ImmutableList.builder();
        int rerollCounter = 0;
        RandomElement currentRoll = rollDie(sides, numberSupplier, rollId, index, rerollCounter++);
        final String rerollValue = String.valueOf(sides);
        resultBuilder.add(currentRoll);
        while (currentRoll.getRollElement().getValue().equals(rerollValue)) {
            currentRoll = rollDie(sides, numberSupplier, rollId, index, rerollCounter++);
            resultBuilder.add(currentRoll);
        }
        return resultBuilder.build();
    }

    public static @NonNull ImmutableList<RandomElement> rollDice(int number, int sides, @NonNull NumberSupplier numberSupplier, @NonNull RollId rollId) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive");
        }
        ImmutableList.Builder<RandomElement> randomElementBuilder = ImmutableList.builder();
        for (int i = 0; i < number; i++) {
            randomElementBuilder.add(rollDie(sides, numberSupplier, rollId, i, 0));
        }
        return randomElementBuilder.build();
    }

    private static RandomElement rollDie(int sides, @NonNull NumberSupplier numberSupplier, @NonNull RollId rollId, int index, int reroll) throws ExpressionException {
        final DieId dieId = DieId.of(rollId, index, reroll);
        final int value = numberSupplier.get(0, sides, dieId);
        return new RandomElement(new RollElement(String.valueOf(value), RollElement.NO_TAG, RollElement.NO_COLOR), 1, sides, dieId);

    }

    public static @NonNull RandomElement pickOneOf(List<RollElement> list, @NonNull NumberSupplier numberSupplier, @NonNull DieId dieId) throws ExpressionException {
        return new RandomElement(list.get(numberSupplier.get(0, list.size(), dieId) - 1), list.stream()
                .map(RollElement::getValue)
                .collect(ImmutableList.toImmutableList()), dieId);
    }

}

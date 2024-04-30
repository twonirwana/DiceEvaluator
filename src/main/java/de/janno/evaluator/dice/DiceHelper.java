package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class DiceHelper {
    public static @NonNull ImmutableList<RandomElement> explodingDice(int number,
                                                                      int sides,
                                                                      @NonNull NumberSupplier numberSupplier,
                                                                      @NonNull RollId rollId,
                                                                      int maxNumberOfElements,
                                                                      String expression) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive", rollId.getExpressionPosition());
        }
        List<RandomElement> resultBuilder = new ArrayList<>();
        //order of the exploded dice is not relevant, the random elements get sorted later
        for (int i = 0; i < number; i++) {
            resultBuilder.addAll(rollExplodingDie(sides, numberSupplier, rollId, i, maxNumberOfElements, expression));
            if (resultBuilder.size() > maxNumberOfElements) {
                throw new ExpressionException("To many elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, resultBuilder.size()), rollId.getExpressionPosition());
            }
        }
        return ImmutableList.copyOf(resultBuilder);
    }

    private static List<RandomElement> rollExplodingDie(int sides, NumberSupplier numberSupplier, RollId rollId, int index, int maxNumberOfElements, String expression) throws ExpressionException {
        List<RandomElement> resultBuilder = new ArrayList<>();
        int rerollCounter = 0;
        RandomElement currentRoll = rollDie(sides, numberSupplier, rollId, index, rerollCounter++);
        final String rerollValue = String.valueOf(sides);
        resultBuilder.add(currentRoll);
        while (currentRoll.getRollElement().getValue().equals(rerollValue)) {
            currentRoll = rollDie(sides, numberSupplier, rollId, index, rerollCounter++);
            resultBuilder.add(currentRoll);
            if (resultBuilder.size() > maxNumberOfElements) {
                throw new ExpressionException("To many elements in roll '%s', max is %d but there where %d".formatted(expression, maxNumberOfElements, resultBuilder.size()), rollId.getExpressionPosition());
            }
        }
        return ImmutableList.copyOf(resultBuilder);
    }

    public static @NonNull ImmutableList<RandomElement> rollDice(int number, int sides, @NonNull NumberSupplier numberSupplier, @NonNull RollId rollId) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive", rollId.getExpressionPosition());
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

package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

public final class DiceHelper {
    public static @NonNull ImmutableList<Integer> explodingDice(int number, int sides, @NonNull NumberSupplier numberSupplier) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive");
        }
        ImmutableList.Builder<Integer> resultBuilder = ImmutableList.builder();
        int diceToRoll = number;
        while (diceToRoll > 0) {
            List<Integer> roll = rollDice(diceToRoll, sides, numberSupplier);
            resultBuilder.addAll(roll);
            diceToRoll = (int) roll.stream().filter(i -> i == sides).count();
        }
        return resultBuilder.build();
    }

    public static @NonNull ImmutableList<RollElement> explodedAddDie2RollElements(@NonNull List<ExplodedAddDie> in) {
        return in.stream()
                .map(ExplodedAddDie::getValue)
                .map(String::valueOf)
                .map(i -> new RollElement(i, RollElement.NO_TAG, RollElement.NO_COLOR))
                .collect(ImmutableList.toImmutableList());
    }

    public static @NonNull ImmutableList<RandomElement> explodedAddDie2RandomElements(@NonNull List<ExplodedAddDie> in) {
        return in.stream()
                .flatMap(r -> r.getDiceThrowResults().stream().map(String::valueOf)
                        .map(i -> new RollElement(i, RollElement.NO_TAG, RollElement.NO_COLOR))
                        .map(re -> new RandomElement(re, 1, r.getSidesOfDie())))
                .collect(ImmutableList.toImmutableList());
    }

    public static @NonNull ImmutableList<RollElement> toRollElements(@NonNull List<Integer> in) {
        return in.stream()
                .map(String::valueOf)
                .map(i -> new RollElement(i, RollElement.NO_TAG, RollElement.NO_COLOR))
                .collect(ImmutableList.toImmutableList());
    }

    public static @NonNull ImmutableList<ExplodedAddDie> explodingAddDice(int number, int sides, @NonNull NumberSupplier numberSupplier) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive");
        }
        ImmutableList.Builder<ExplodedAddDie> resultBuilder = ImmutableList.builder();
        for (int i = 0; i < number; i++) {
            resultBuilder.add(rollExplodingAddDie(sides, numberSupplier));
        }
        return resultBuilder.build();
    }

    public static ExplodedAddDie rollExplodingAddDie(int sides, @NonNull NumberSupplier numberSupplier) throws ExpressionException {
        int current = numberSupplier.get(0, sides);
        int res = current;
        ImmutableList.Builder<Integer> resultBuilder = ImmutableList.builder();
        resultBuilder.add(current);
        while (current == sides) {
            current = numberSupplier.get(0, sides);
            res += current;
            resultBuilder.add(current);
        }
        return new ExplodedAddDie(res, sides, resultBuilder.build());
    }

    public static @NonNull ImmutableList<Integer> rollDice(int number, int sides, @NonNull NumberSupplier numberSupplier) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive");
        }
        ImmutableList.Builder<Integer> resultBuilder = ImmutableList.builder();
        for (int i = 0; i < number; i++) {
            resultBuilder.add(numberSupplier.get(0, sides));
        }
        return resultBuilder.build();
    }

    public static @NonNull RollElement pickOneOf(List<RollElement> list, @NonNull NumberSupplier numberSupplier) throws ExpressionException {
        return list.get(numberSupplier.get(0, list.size()) - 1);
    }

    @Value
    public static class ExplodedAddDie {
        int value;
        int sidesOfDie;
        ImmutableList<Integer> diceThrowResults;
    }
}

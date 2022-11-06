package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

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

    public static @NonNull ImmutableList<RollElement> toRollElements(@NonNull List<Integer> in) {
        return in.stream()
                .map(String::valueOf)
                .map(i -> new RollElement(i, RollElement.NO_COLOR))
                .collect(ImmutableList.toImmutableList());
    }

    public static @NonNull ImmutableList<Integer> explodingAddDice(int number, int sides, @NonNull NumberSupplier numberSupplier) throws ExpressionException {
        if (sides == 0) {
            return ImmutableList.of();
        }
        if (sides < 0) {
            throw new ExpressionException("Sides of dice to roll must be positive");
        }
        ImmutableList.Builder<Integer> resultBuilder = ImmutableList.builder();
        for (int i = 0; i < number; i++) {
            resultBuilder.add(rollExplodingAddDie(sides, numberSupplier));
        }
        return resultBuilder.build();
    }


    public static int rollExplodingAddDie(int sides, @NonNull NumberSupplier numberSupplier) throws ExpressionException {
        int current = numberSupplier.get(0, sides);
        int res = current;
        while (current == sides) {
            current = numberSupplier.get(0, sides);
            res += current;
        }
        return res;
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
}

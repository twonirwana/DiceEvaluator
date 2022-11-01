package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.List;
import java.util.stream.IntStream;

public final class DiceHelper {
    public static @NonNull ImmutableList<Integer> explodingDice(int number, int sides, @NonNull NumberSupplier numberSupplier) {
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

    public static @NonNull ImmutableList<Integer> explodingAddDice(int number, int sides, @NonNull NumberSupplier numberSupplier) {
        return IntStream.range(0, number)
                .mapToObj(i -> rollExplodingAddDie(sides, numberSupplier))
                .collect(ImmutableList.toImmutableList());
    }


    public static int rollExplodingAddDie(int sides, @NonNull NumberSupplier numberSupplier) {
        int current = numberSupplier.get(0, sides);
        int res = current;
        while (current == sides) {
            current = numberSupplier.get(0, sides);
            res += current;
        }
        return res;
    }

    public static @NonNull ImmutableList<Integer> rollDice(int number, int sides, @NonNull NumberSupplier numberSupplier) {
        return IntStream.range(0, number)
                .mapToObj(i -> numberSupplier.get(0, sides))
                .collect(ImmutableList.toImmutableList());
    }

    public static @NonNull RollElement pickOneOf(List<RollElement> list, @NonNull NumberSupplier numberSupplier) {
        return list.get(numberSupplier.get(0, list.size()) - 1);
    }
}

package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.DiceHelper.explodingDice;
import static de.janno.evaluator.dice.DiceHelper.toRollElements;
import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class ExplodingDice extends Operator {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public ExplodingDice(NumberSupplier numberSupplier, int maxNumberOfDice) {
        super(ImmutableSet.of("d!", "D!"), Operator.Associativity.RIGHT, getOderNumberOf(ExplodingDice.class), Operator.Associativity.LEFT, getOderNumberOf(ExplodingDice.class));
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = rollAllSupplier(operands, constants);
            UniqueRandomElements.Builder randomElements = UniqueRandomElements.builder();
            if (rolls.size() == 1) {
                final Roll right = rolls.get(0);
                final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
                if (sidesOfDie < 2) {
                    throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
                }
                final ImmutableList<RollElement> rollElements = toRollElements(explodingDice(1, sidesOfDie, numberSupplier));
                randomElements.add(right.getRandomElementsInRoll());

                randomElements.addAsRandomElements(rollElements.stream()
                        .map(r -> new RandomElement(r, 1, sidesOfDie))
                        .collect(ImmutableList.toImmutableList()));

                return new Roll(getRightUnaryExpression(getPrimaryName(), rolls),
                        rollElements,
                        randomElements.build(),
                        ImmutableList.of(right));
            }

            final Roll left = rolls.get(0);
            final Roll right = rolls.get(1);
            randomElements.add(left.getRandomElementsInRoll());

            randomElements.add(right.getRandomElementsInRoll());

            final int numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
            if (numberOfDice > maxNumberOfDice) {
                throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
            }
            if (numberOfDice < 0) {
                throw new ExpressionException(String.format("The number of dice can not be negativ but was %d", numberOfDice));
            }
            final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            if (sidesOfDie < 2) {
                throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
            }
            final ImmutableList<RollElement> rollElements = toRollElements(explodingDice(numberOfDice, sidesOfDie, numberSupplier));

            randomElements.addAsRandomElements(rollElements.stream()
                    .map(r -> new RandomElement(r, 1, sidesOfDie))
                    .collect(ImmutableList.toImmutableList()));
            return new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    rollElements,
                    randomElements.build(),
                    ImmutableList.of(left, right));
        };
    }
}

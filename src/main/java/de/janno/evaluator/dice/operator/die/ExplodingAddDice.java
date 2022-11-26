package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.RandomElement;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.RollOperator;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.DiceHelper.explodingAddDice;
import static de.janno.evaluator.dice.DiceHelper.toRollElements;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class ExplodingAddDice extends RollOperator {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public ExplodingAddDice(NumberSupplier numberSupplier, int maxNumberOfDice) {
        super(ImmutableSet.of("d!!", "D!!"), Operator.Associativity.RIGHT, getOderNumberOf(ExplodingAddDice.class), Operator.Associativity.LEFT, getOderNumberOf(ExplodingAddDice.class));
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        ImmutableList.Builder<ImmutableList<RandomElement>> randomElements = ImmutableList.builder();
        if (operands.size() == 1) {
            final Roll right = operands.get(0);
            final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            if (sidesOfDie < 2) {
                throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
            }
            final ImmutableList<RollElement> rollElements = toRollElements(explodingAddDice(1, sidesOfDie, numberSupplier));
            if (right.getRandomElementsInRoll().size() > 0) {
                randomElements.addAll(right.getRandomElementsInRoll());
            }
            randomElements.add(rollElements.stream()
                    .map(r -> new RandomElement(r.getValue(), 1, sidesOfDie))
                    .collect(ImmutableList.toImmutableList()));
            return new Roll(getRightUnaryExpression(getPrimaryName(), operands),
                    rollElements,
                    randomElements.build(),
                    ImmutableList.of(right));
        }

        final Roll left = operands.get(0);
        final Roll right = operands.get(1);
        if (left.getRandomElementsInRoll().size() > 0) {
            randomElements.addAll(left.getRandomElementsInRoll());
        }
        if (right.getRandomElementsInRoll().size() > 0) {
            randomElements.addAll(right.getRandomElementsInRoll());
        }
        final int numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
        if (Math.abs(numberOfDice) > maxNumberOfDice) {
            throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
        }
        final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        if (sidesOfDie < 2) {
            throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
        }
        final ImmutableList<RollElement> rollElements = toRollElements(explodingAddDice(numberOfDice, sidesOfDie, numberSupplier));

        randomElements.add(rollElements.stream()
                .map(r -> new RandomElement(r.getValue(), 1, sidesOfDie))
                .collect(ImmutableList.toImmutableList()));
        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                rollElements,
                randomElements.build(),
                ImmutableList.of(left, right));
    }
}

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

import static de.janno.evaluator.dice.DiceHelper.*;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class RegularDice extends RollOperator {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public RegularDice(NumberSupplier numberSupplier, int maxNumberOfDice) {
        super(ImmutableSet.of("d", "D"), Operator.Associativity.RIGHT, getOderNumberOf(RegularDice.class), Operator.Associativity.LEFT, getOderNumberOf(RegularDice.class));
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {

        final int numberOfDice;
        final Roll right;
        final ImmutableList<Roll> childrenRolls;
        final String expression;
        ImmutableList.Builder<ImmutableList<RandomElement>> randomElements = ImmutableList.builder();
        if (operands.size() == 1) {
            right = operands.get(0);
            numberOfDice = 1;
            childrenRolls = ImmutableList.of(right);
            expression = getRightUnaryExpression(getPrimaryName(), operands);
        } else if (operands.size() == 2) {
            Roll left = operands.get(0);
            right = operands.get(1);
            childrenRolls = ImmutableList.of(left, right);
            numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
            expression = getBinaryOperatorExpression(getPrimaryName(), operands);
            if (left.getRandomElementsInRoll().size() > 0) {
                randomElements.addAll(left.getRandomElementsInRoll());
            }
        } else {
            throw new IllegalStateException("More then two operands for " + getName());
        }
        if (right.getRandomElementsInRoll().size() > 0) {
            randomElements.addAll(right.getRandomElementsInRoll());
        }
        if (Math.abs(numberOfDice) > maxNumberOfDice) {
            throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
        }
        final ImmutableList<RollElement> rollElements;
        if (right.asInteger().isPresent()) {
            int sidesOfDie = right.asInteger().get();
            rollElements = toRollElements(rollDice(numberOfDice, sidesOfDie, numberSupplier));

            randomElements.add(rollElements.stream()
                    .map(r -> new RandomElement(r.getValue(), 1, sidesOfDie))
                    .collect(ImmutableList.toImmutableList()));
        } else {
            ImmutableList.Builder<RollElement> builder = ImmutableList.builder();
            for (int i = 0; i < numberOfDice; i++) {
                builder.add(pickOneOf(right.getElements(), numberSupplier));
            }
            rollElements = builder.build();
            randomElements.add(rollElements.stream()
                    .map(r -> new RandomElement(r.getValue(), right.getElements().stream()
                            .map(RollElement::getValue)
                            .collect(ImmutableList.toImmutableList())))
                    .collect(ImmutableList.toImmutableList()));
        }

        return new Roll(expression,
                rollElements,
                randomElements.build(),
                childrenRolls);
    }
}

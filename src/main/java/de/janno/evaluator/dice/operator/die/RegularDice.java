package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.NumberSupplier;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.RollOperator;
import lombok.NonNull;

import java.util.List;
import java.util.stream.IntStream;

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
        } else {
            throw new IllegalStateException("More then two operands for " + getName());
        }
        if (numberOfDice > maxNumberOfDice) {
            throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
        }
        final ImmutableList<RollElement> rollElements;
        if (right.asInteger().isPresent()) {
            int sidesOfDie = right.asInteger().get();
            rollElements = toRollElements(rollDice(numberOfDice, sidesOfDie, numberSupplier));
        } else {
            rollElements = IntStream.range(0, numberOfDice)
                    .mapToObj(i -> pickOneOf(right.getElements(), numberSupplier))
                    .collect(ImmutableList.toImmutableList());
        }
        return new Roll(expression,
                rollElements,
                ImmutableList.of(rollElements),
                childrenRolls);
    }
}

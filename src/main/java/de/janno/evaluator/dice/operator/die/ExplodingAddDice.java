package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.random.NumberSupplier;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.RollOperator;
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
        if (operands.size() == 1) {
            Roll right = operands.get(0);
            int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            ImmutableList<RollElement> diceRoll = toRollElements(explodingAddDice(1, sidesOfDie, numberSupplier));
            return new Roll(getRightUnaryExpression(getPrimaryName(), operands),
                    diceRoll,
                    ImmutableList.of(diceRoll),
                    ImmutableList.of(right));
        }

        Roll left = operands.get(0);
        Roll right = operands.get(1);
        int numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
        if (Math.abs(numberOfDice) > maxNumberOfDice) {
            throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
        }
        int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        ImmutableList<RollElement> diceResult = toRollElements(explodingAddDice(numberOfDice, sidesOfDie, numberSupplier));
        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                diceResult,
                ImmutableList.of(diceResult),
                ImmutableList.of(left, right));
    }
}

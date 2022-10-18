package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.NumberSupplier;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.EvaluationHelper.explodingDice;
import static de.janno.evaluator.dice.EvaluationHelper.toResultElements;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class ExplodingDice extends Operator<Result> {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public ExplodingDice(NumberSupplier numberSupplier, int maxNumberOfDice) {
        super(ImmutableSet.of("d!", "D!"), Operator.Associativity.RIGHT, getOderNumberOf(ExplodingDice.class), Operator.Associativity.LEFT, getOderNumberOf(ExplodingDice.class));
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {

        if (operands.size() == 1) {
            Result right = operands.get(0);
            int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            ImmutableList<ResultElement> diceResult = toResultElements(explodingDice(1, sidesOfDie, numberSupplier));
            return new Result(ResultUtil.getRightUnaryExpression(getPrimaryName(), operands),
                    diceResult,
                    ImmutableList.of(diceResult),
                    ImmutableList.of(right));
        }

        Result left = operands.get(0);
        Result right = operands.get(1);
        int numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
        if (numberOfDice > maxNumberOfDice) {
            throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
        }
        int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        ImmutableList<ResultElement> diceResult = toResultElements(explodingDice(numberOfDice, sidesOfDie, numberSupplier));
        return new Result(ResultUtil.getBinaryOperatorExpression(getPrimaryName(), operands),
                diceResult,
                ImmutableList.of(diceResult),
                ImmutableList.of(left, right));
    }
}

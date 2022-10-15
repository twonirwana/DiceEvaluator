package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.NumberSupplier;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import lombok.NonNull;

import java.util.List;
import java.util.stream.IntStream;

import static de.janno.evaluator.dice.EvaluationHelper.*;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class RegularDice extends Operator<Result> {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public RegularDice(NumberSupplier numberSupplier, int maxNumberOfDice) {
        super(ImmutableSet.of("d", "D"), Operator.Associativity.RIGHT, getOderNumberOf(RegularDice.class), Operator.Associativity.LEFT, getOderNumberOf(RegularDice.class));
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {

        final int numberOfDice;
        final Result right;
        final ImmutableList<Result> childrenResults;
        if (operands.size() == 1) {
            right = operands.get(0);
            numberOfDice = 1;
            childrenResults = ImmutableList.of(right);
        } else if (operands.size() == 2) {
            Result left = operands.get(0);
            right = operands.get(1);
            childrenResults = ImmutableList.of(left, right);
            numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
        } else {
            throw new ExpressionException("More then two operands for " + getName());
        }
        if (numberOfDice > maxNumberOfDice) {
            throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
        }
        final ImmutableList<ResultElement> diceResult;
        if (right.asInteger().isPresent()) {
            int sidesOfDie = right.asInteger().get();
            diceResult = toResultElements(rollDice(numberOfDice, sidesOfDie, numberSupplier));
            return new Result(getName(),
                    diceResult,
                    ImmutableList.of(diceResult),
                    childrenResults);
        } else {
            diceResult = IntStream.range(0, numberOfDice)
                    .mapToObj(i -> pickOneOf(right.getElements(), numberSupplier))
                    .collect(ImmutableList.toImmutableList());
        }
        return new Result(getName(),
                diceResult,
                ImmutableList.of(diceResult),
                childrenResults);
    }
}

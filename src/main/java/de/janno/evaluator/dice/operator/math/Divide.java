package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkAllElementsAreSameColor;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Divide extends Operator<Result> {

    public Divide() {
        super("/", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Divide.class));
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {
        Result left = operands.get(0);
        Result right = operands.get(1);
        checkAllElementsAreSameColor(getName(), left, right);
        final int leftNumber = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
        final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));

        final ImmutableList<ResultElement> res = ImmutableList.of(new ResultElement(String.valueOf(Math.divideExact(leftNumber, rightNumber)), left.getElements().get(0).getColor()));
        return new Result(getName(),
                res,
                ImmutableList.<ImmutableList<ResultElement>>builder()
                        .addAll(left.getRandomElementsProducingTheResult())
                        .addAll(right.getRandomElementsProducingTheResult())
                        .build(),
                ImmutableList.of(left, right)
        );
    }
}

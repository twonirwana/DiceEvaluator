package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class GreaterEqualThanFilter extends Operator<Result> {

    public GreaterEqualThanFilter() {
        super(">=", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(GreaterEqualThanFilter.class));
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {

        Result left = operands.get(0);
        Result right = operands.get(1);
        checkContainsOnlyInteger(getName(), left, "left");
        final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        //todo color only filtered by same color?
        ImmutableList<ResultElement> diceResult = left.getElements().stream()
                .filter(i -> i.asInteger().isPresent() && i.asInteger().get() >= rightNumber)
                .collect(ImmutableList.toImmutableList());
        return new Result(ResultUtil.getBinaryOperatorExpression(getPrimaryName(), operands),
                diceResult,
                ImmutableList.<ImmutableList<ResultElement>>builder()
                        .addAll(left.getRandomElementsProducingTheResult())
                        .addAll(right.getRandomElementsProducingTheResult())
                        .build(),
                ImmutableList.of(left, right)
        );
    }
}

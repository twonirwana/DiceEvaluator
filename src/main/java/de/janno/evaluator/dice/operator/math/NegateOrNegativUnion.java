package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;

public final class NegateOrNegativUnion extends Operator<Result> {
    public NegateOrNegativUnion() {
        super(ImmutableSet.of("-"), Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(NegateOrNegativUnion.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(NegateOrNegativUnion.class));
    }


    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {
        if (operands.size() == 1) {
            Result right = operands.get(0);
            checkContainsOnlyInteger(getName(), right, "right");
            ImmutableList<ResultElement> negated = right.getElements().stream()
                    .map(e -> new ResultElement(String.valueOf(e.asInteger().orElseThrow() * -1), e.getColor()))
                    .collect(ImmutableList.toImmutableList());
            return new Result(ResultUtil.getRightUnaryExpression(getPrimaryName(), operands),
                    negated,
                    right.getRandomElementsProducingTheResult(),
                    ImmutableList.of(right));
        }

        Result left = operands.get(0);
        Result right = operands.get(1);
        checkContainsOnlyInteger(getName(), right, "right");
        final ImmutableList<ResultElement> res = ImmutableList.<ResultElement>builder()
                .addAll(left.getElements())
                .addAll(right.getElements().stream()
                        .map(e -> new ResultElement(String.valueOf(e.asInteger().orElseThrow() * -1), e.getColor()))
                        .toList()
                ).build();

        return new Result(ResultUtil.getBinaryOperatorExpression(getPrimaryName(), operands),
                res,
                ImmutableList.<ImmutableList<ResultElement>>builder()
                        .addAll(left.getRandomElementsProducingTheResult())
                        .addAll(right.getRandomElementsProducingTheResult())
                        .build(),
                ImmutableList.of(left, right)
        );
    }


}

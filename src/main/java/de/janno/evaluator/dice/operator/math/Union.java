package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Union extends Operator<Result> {
    public Union() {
        super("+", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Union.class));
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) {
        Result left = operands.get(0);
        Result right = operands.get(1);
        final ImmutableList<ResultElement> res = ImmutableList.<ResultElement>builder()
                .addAll(left.getElements())
                .addAll(right.getElements())
                .build();
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

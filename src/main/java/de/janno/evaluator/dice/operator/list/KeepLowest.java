package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepLowest extends Operator<Result> {

    public KeepLowest() {
        super("l", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(KeepLowest.class));
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {
        Result left = operands.get(0);
        Result right = operands.get(1);
        checkContainsOnlyInteger(getName(), left, "left");
        final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        //todo right color only filtered by same color?
        ImmutableList<ResultElement> keep = left.getElements().stream()
                .collect(Collectors.groupingBy(ResultElement::getColor)).values().stream()
                .flatMap(cl -> cl.stream()
                        .sorted()
                        .limit(rightNumber)
                )
                .collect(ImmutableList.toImmutableList());
        return new Result(getName(),
                keep,
                ImmutableList.<ImmutableList<ResultElement>>builder()
                        .addAll(left.getRandomElementsProducingTheResult())
                        .addAll(right.getRandomElementsProducingTheResult())
                        .build(),
                ImmutableList.of(left, right));
    }
}

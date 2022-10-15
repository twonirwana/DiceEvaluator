package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Sum extends Operator<Result> {

    public Sum() {
        super("=", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, getOderNumberOf(Sum.class));
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {
        Result left = operands.get(0);
        checkContainsOnlyInteger(getName(), left, "left");

        ImmutableList<ResultElement> res = left.getElements().stream().collect(Collectors.groupingBy(ResultElement::getColor)).entrySet().stream()
                .map(e -> new ResultElement(String.valueOf(e.getValue().stream()
                        .map(ResultElement::asInteger)
                        .flatMap(Optional::stream)
                        .mapToInt(Integer::valueOf).sum()), e.getKey()))
                .collect(ImmutableList.toImmutableList());

        return new Result(getName(),
                res,
                left.getRandomElementsProducingTheResult(),
                ImmutableList.of(left)
        );
    }
}

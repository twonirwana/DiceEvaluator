package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.ResultElement;
import de.janno.evaluator.dice.ResultUtil;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Count extends Operator<Result> {

    public Count() {
        super(Set.of("c", "C"), Operator.Associativity.LEFT, getOderNumberOf(Count.class), null, null);
    }

    @Override
    public @NonNull Result evaluate(@NonNull List<Result> operands) throws ExpressionException {

        Result left = operands.get(0);
        //count of each color separate
        ImmutableList<ResultElement> res;
        if (operands.stream().mapToLong(result -> result.getElements().size()).sum() == 0) {
            res = ImmutableList.of(new ResultElement("0", ResultElement.NO_COLOR));
        } else {
            res = left.getElements().stream()
                    .collect(Collectors.groupingBy(ResultElement::getColor)).entrySet().stream()
                    .map(e -> new ResultElement(String.valueOf(e.getValue().size()), e.getKey()))
                    .collect(ImmutableList.toImmutableList());
        }
        return new Result(ResultUtil.getLeftUnaryExpression(getPrimaryName(), operands),
                res,
                left.getRandomElementsProducingTheResult(),
                ImmutableList.of(left)
        );
    }
}

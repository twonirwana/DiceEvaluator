package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Operator;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Sum extends Operator {

    public Sum() {
        super("=", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, getOderNumberOf(Sum.class));
    }

    private static int sumExact(List<RollElement> elements) {
        return elements.stream()
                .map(RollElement::asInteger)
                .flatMap(Optional::stream)
                .reduce(0, Math::addExact);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        Roll left = operands.get(0);
        checkContainsOnlyInteger(getName(), left, "left");


        ImmutableList<RollElement> res = left.getElements().stream().collect(Collectors.groupingBy(RollElement::getColor)).entrySet().stream()
                .map(e -> new RollElement(String.valueOf(sumExact(e.getValue())), e.getKey()))
                .collect(ImmutableList.toImmutableList());

        return new Roll(getLeftUnaryExpression(getPrimaryName(), operands),
                res,
                left.getRandomElementsInRoll(),
                ImmutableList.of(left), null
        );
    }
}

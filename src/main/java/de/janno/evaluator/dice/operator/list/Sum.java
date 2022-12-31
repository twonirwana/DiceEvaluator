package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
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
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(getName(), rolls, 1,1);

            Roll left = rolls.get(0);
            checkContainsOnlyInteger(getName(), left, "left");


            ImmutableList<RollElement> res = left.getElements().stream().collect(Collectors.groupingBy(RollElement::getColor)).entrySet().stream()
                    .map(e -> new RollElement(String.valueOf(sumExact(e.getValue())), e.getKey()))
                    .collect(ImmutableList.toImmutableList());

            return ImmutableList.of(new Roll(getLeftUnaryExpression(getName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left)));
        };
    }
}

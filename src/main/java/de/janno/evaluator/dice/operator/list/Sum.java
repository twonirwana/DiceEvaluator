package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Sum extends Operator {

    public Sum() {
        super("=", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, getOderNumberOf(Sum.class));
    }

    private static BigDecimal sumExact(List<RollElement> elements) {
        return elements.stream()
                .map(RollElement::asDecimal)
                .flatMap(Optional::stream)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(operands, variables);
            checkRollSize(inputValue, rolls, 1,1);

            Roll left = rolls.get(0);
            checkContainsOnlyDecimal(inputValue, left, "left");

            ImmutableList<RollElement> res = left.getElements().stream().collect(Collectors.groupingBy(RollElement::getTag)).entrySet().stream()
                    .map(e -> new RollElement(sumExact(e.getValue()).stripTrailingZeros().stripTrailingZeros().toPlainString(), e.getKey(), RollElement.NO_COLOR))
                    .collect(ImmutableList.toImmutableList());

            return ImmutableList.of(new Roll(getLeftUnaryExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left)));
        };
    }
}

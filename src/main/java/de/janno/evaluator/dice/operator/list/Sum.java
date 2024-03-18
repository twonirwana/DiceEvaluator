package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyDecimal;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Sum extends Operator {

    public Sum(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("=", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, getOderNumberOf(Sum.class), maxNumberOfElements, keepChildrenRolls);
    }

    private static BigDecimal sumExact(List<RollElement> elements) {
        return elements.stream()
                .map(RollElement::asDecimal)
                .flatMap(Optional::stream)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variables) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, variables);
                checkRollSize(inputValue, rolls, 1, 1);

                Roll left = rolls.getFirst();
                checkContainsOnlyDecimal(inputValue, left, "left");

                ImmutableList<RollElement> res = left.getElements().stream().collect(Collectors.groupingBy(RollElement::getTag)).entrySet().stream()
                        .map(e -> new RollElement(sumExact(e.getValue()).stripTrailingZeros().stripTrailingZeros().toPlainString(), e.getKey(), RollElement.NO_COLOR))
                        .collect(ImmutableList.toImmutableList());

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(left),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getLeftUnaryExpression(inputValue, operands);
            }
        };
    }
}

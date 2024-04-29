package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.*;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyDecimal;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public final class NegateAddRemove extends Operator {
    private final static BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);

    public NegateAddRemove(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("-", Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(NegateAddRemove.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(NegateAddRemove.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition, rolls, 1, 2);

                if (rolls.size() == 1) {
                    Roll right = rolls.getFirst();
                     checkContainsOnlyDecimal(expressionPosition, right, "right");
                    ImmutableList<RollElement> negated = right.getElements().stream()
                            .map(e -> new RollElement(e.asDecimal().orElseThrow().multiply(MINUS_ONE).stripTrailingZeros().toPlainString(), e.getTag(), e.getColor()))
                            .collect(ImmutableList.toImmutableList());
                    return Optional.of(ImmutableList.of(new Roll(toExpression(),
                            negated,
                            RandomElementsBuilder.fromRolls(rolls),
                            ImmutableList.of(right),
                            expressionPosition,
                            maxNumberOfElements, keepChildrenRolls)));
                }

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);


                ImmutableList.Builder<RollElement> resultBuilder = ImmutableList.builder();

                List<RollElement> toRemove = new ArrayList<>(right.getElements());
                for (RollElement rollElement : left.getElements()) {
                    Optional<RollElement> matchingElement = toRemove.stream()
                            .filter((r -> r.isEqualValueAndTag(rollElement)))
                            .findFirst();
                    if (matchingElement.isPresent()) {
                        toRemove.remove(matchingElement.get());
                    } else {
                        resultBuilder.add(rollElement);
                    }
                }

                if (toRemove.stream().anyMatch(r -> r.asDecimal().isEmpty())) {
                    throw new ExpressionException(String.format("'%s' requires as right input only decimals or elements that are on the left side '%s' but was '%s'",
                            expressionPosition.getValue(),
                            left.getElements().stream().map(RollElement::getValue).toList(),
                            right.getElements().stream().map(RollElement::getValue).toList()), expressionPosition);
                }

                resultBuilder.addAll(toRemove.stream()
                        .map(e -> new RollElement(e.asDecimal().orElseThrow().multiply(MINUS_ONE).stripTrailingZeros().toPlainString(), e.getTag(), e.getColor()))
                        .toList());

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        resultBuilder.build(),
                        RandomElementsBuilder.fromRolls(rolls),
                        ImmutableList.of(left, right),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                if (operands.size() == 1) {
                    return getRightUnaryExpression(expressionPosition, operands);
                }

                return getBinaryOperatorExpression(expressionPosition, operands);
            }
        };
    }


}

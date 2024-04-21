package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepHighest extends Operator {

    public KeepHighest(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("k", null, null, Operator.Associativity.LEFT, getOderNumberOf(KeepHighest.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition.getValue(), rolls, 2, 2);

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);
                final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.getValue(), right, "right"));
                if (rightNumber < 0) {
                    throw new ExpressionException(String.format("The number to keep can not be negativ but was %d", rightNumber));
                }
                final String rightTag = right.getElements().getFirst().getTag();
                ImmutableList<RollElement> otherTagElements = left.getElements().stream()
                        .filter(r -> !r.getTag().equals(rightTag))
                        .collect(ImmutableList.toImmutableList());

                ImmutableList<RollElement> keep = left.getElements().stream()
                        .filter(r -> r.getTag().equals(rightTag))
                        .collect(Collectors.groupingBy(RollElement::getTag)).values().stream()
                        .flatMap(cl -> cl.stream()
                                .sorted(Comparator.reverseOrder())
                                .limit(rightNumber)
                        )
                        .collect(ImmutableList.toImmutableList());
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        ImmutableList.<RollElement>builder()
                                .addAll(keep)
                                .addAll(otherTagElements)
                                .build(),
                        RandomElementsBuilder.fromRolls(rolls),
                        ImmutableList.of(left, right),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(expressionPosition.getValue(), operands);
            }
        };
    }
}

package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.operator.OperatorOrder;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public final class AddToList extends Operator {
    public AddToList(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("+", Operator.Associativity.RIGHT, OperatorOrder.getOderNumberOf(AddToList.class), Operator.Associativity.LEFT, OperatorOrder.getOderNumberOf(AddToList.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition, rolls, 1, 2);

                if (rolls.size() == 1) {
                    return Optional.of(ImmutableList.of(new Roll(toExpression(),
                            rolls.getFirst().getElements(),
                            RandomElementsBuilder.fromRolls(rolls),
                            ImmutableList.of(rolls.getFirst()),
                            expressionPosition,
                            maxNumberOfElements, keepChildrenRolls)));
                }

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);
                final ImmutableList<RollElement> res = ImmutableList.<RollElement>builder()
                        .addAll(left.getElements())
                        .addAll(right.getElements())
                        .build();
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
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

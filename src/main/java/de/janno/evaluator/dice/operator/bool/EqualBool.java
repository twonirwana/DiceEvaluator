package de.janno.evaluator.dice.operator.bool;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class EqualBool extends Operator {

    public EqualBool(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("=?", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(EqualBool.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition, rolls, 2, 2);

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);

                final boolean isEqual = left.equalForValueAndTag(right);
                ImmutableList<RollElement> diceResult = ImmutableList.of(new RollElement(String.valueOf(isEqual), RollElement.NO_TAG, RollElement.NO_COLOR));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        diceResult,
                        RandomElementsBuilder.fromRolls(rolls, rollContext),
                        ImmutableList.of(left, right),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(expressionPosition, operands);
            }
        };
    }
}

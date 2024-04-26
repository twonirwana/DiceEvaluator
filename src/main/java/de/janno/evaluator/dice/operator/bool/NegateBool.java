package de.janno.evaluator.dice.operator.bool;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotBoolean;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class NegateBool extends Operator {

    public NegateBool(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("!", OperatorType.UNARY, Associativity.RIGHT, getOderNumberOf(NegateBool.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition, rolls, 1, 1);

                Roll value = rolls.getFirst();

                final boolean boolValue = value.asBoolean().orElseThrow(() ->  throwNotBoolean(expressionPosition, value, "right"));

                ImmutableList<RollElement> diceResult = ImmutableList.of(new RollElement(String.valueOf((!boolValue)), RollElement.NO_TAG, RollElement.NO_COLOR));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        diceResult,
                        RandomElementsBuilder.fromRolls(rolls),
                        ImmutableList.of(value),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getRightUnaryExpression(expressionPosition, operands);
            }
        };
    }
}

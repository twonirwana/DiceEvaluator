package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class IntegerDivide extends Operator {

    public IntegerDivide(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("/", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(IntegerDivide.class), maxNumberOfElements, keepChildrenRolls);
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
                checkAllElementsAreSameTag(expressionPosition, left, right);
                final int leftNumber = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition, left, "left"));
                final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition, right, "right"));
                final int quotient;
                try {
                    quotient = Math.divideExact(leftNumber, rightNumber);
                } catch (ArithmeticException e) {
                    throw new ExpressionException(e.getMessage(), expressionPosition);
                }
                final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(String.valueOf(quotient), left.getElements().getFirst().getTag(), RollElement.NO_COLOR));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
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

package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Multiply extends Operator {
    public Multiply(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("*", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Multiply.class), maxNumberOfElements, keepChildrenRolls);
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
                checkAllElementsAreSameTag(expressionPosition.getValue(), left, right);
                final BigDecimal leftNumber = left.asDecimal().orElseThrow(() -> throwNotDecimalExpression(expressionPosition.getValue(), left, "left"));
                final BigDecimal rightNumber = right.asDecimal().orElseThrow(() -> throwNotDecimalExpression(expressionPosition.getValue(), right, "right"));
                final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(leftNumber.multiply(rightNumber).stripTrailingZeros().toPlainString(), left.getElements().getFirst().getTag(), RollElement.NO_COLOR));

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
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

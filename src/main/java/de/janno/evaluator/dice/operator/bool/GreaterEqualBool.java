package de.janno.evaluator.dice.operator.bool;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotDecimalExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class GreaterEqualBool extends Operator {

    public GreaterEqualBool(int maxNumberOfElements, boolean keepChildrenRolls) {
        super(">=?", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(GreaterEqualBool.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variables) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, variables);
                checkRollSize(inputValue, rolls, 2, 2);

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);
                final BigDecimal leftNumber = left.asDecimal().orElseThrow(() -> throwNotDecimalExpression(inputValue, left, "left"));
                final BigDecimal rightNumber = right.asDecimal().orElseThrow(() -> throwNotDecimalExpression(inputValue, right, "right"));

                ImmutableList<RollElement> diceResult = ImmutableList.of(new RollElement(String.valueOf(leftNumber.compareTo(rightNumber) >= 0), RollElement.NO_TAG, RollElement.NO_COLOR));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        diceResult,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(left, right),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(getName(), operands);
            }
        };
    }
}

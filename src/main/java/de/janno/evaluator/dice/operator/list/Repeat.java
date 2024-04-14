package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Repeat extends Operator {

    public Repeat(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("x", null, null, Associativity.LEFT, getOderNumberOf(Repeat.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> leftRolls = operands.getFirst().extendRoll(rollContext).orElse(Collections.emptyList());
                checkRollSize(expressionPosition.value(), leftRolls, 1, 1);
                int left = leftRolls.getFirst().asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.value(), leftRolls.getFirst(), "left"));
                if (left > 10 || left < 1) {
                    throw new ExpressionException(String.format("The number of repeat must between 1-10 but was %d", left));
                }
                RollBuilder right = operands.get(1);
                ImmutableList.Builder<Roll> builder = ImmutableList.builder();
                for (int i = 0; i < left; i++) {
                    List<Roll> rightRoll = right.extendRoll(rollContext).orElse(Collections.emptyList());
                    checkRollSize(expressionPosition.value(), rightRoll, 1, 1);
                    builder.addAll(rightRoll);
                }
                return Optional.of(builder.build());
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(expressionPosition.value(), operands);
            }
        };
    }

}

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

public class RepeatList extends Operator {

    public RepeatList(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("r", null, null, Associativity.LEFT, getOderNumberOf(RepeatList.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> leftRolls = operands.getFirst().extendRoll(rollContext).orElse(Collections.emptyList());
                checkRollSize(expressionPosition, leftRolls, 1, 1);
                int left = leftRolls.getFirst().asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition, leftRolls.getFirst(), "left"));
                if (left > 20 || left < 0) {
                    throw new ExpressionException(String.format("The number of list repeat must between 0-20 but was %d", left), expressionPosition);
                }
                if (left == 0) {
                    return Optional.of(List.of());
                }

                RollBuilder right = operands.get(1);


                ImmutableList.Builder<Roll> builder = ImmutableList.builder();
                for (int i = 0; i < left; i++) {
                    List<Roll> rightRoll = right.extendRoll(rollContext).orElse(Collections.emptyList());
                    checkRollSize(expressionPosition, rightRoll, 1, 1);
                    builder.addAll(rightRoll);
                }
                ImmutableList<Roll> rolls = builder.build();


                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        rolls.stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList()),
                        RandomElementsBuilder.fromRolls(rolls, rollContext),
                        rolls,
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

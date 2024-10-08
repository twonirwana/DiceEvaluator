package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Value extends Function {
    public Value(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("val", 2, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                RollContext nameContext = rollContext.copyWithEmptyVariables(); //don't replace literals in the first argument of the function, but it can use new variables
                Optional<List<Roll>> valNameRoll = arguments.getFirst().extendRoll(nameContext);
                if (valNameRoll.isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", expressionPosition.getValue()), expressionPosition);
                }
                ImmutableList.Builder<Roll> rollBuilder = ImmutableList.<Roll>builder()
                        .addAll(valNameRoll.get());
                rollContext.merge(nameContext);
                List<RollBuilder> remainingRollBuilder = arguments.subList(1, arguments.size());
                List<Roll> remainingRolls = RollBuilder.extendAllBuilder(remainingRollBuilder, rollContext);

                List<Roll> rolls = rollBuilder.addAll(remainingRolls).build();

                checkRollSize(expressionPosition, rolls, getMinArgumentCount(), getMaxArgumentCount());
                if (rolls.getFirst().getElements().isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", expressionPosition.getValue()), expressionPosition);
                }
                String valName = rolls.getFirst().asSingleValue().orElseThrow();

                String expression = toExpression();
                rollContext.putVariable(valName, new Roll(expression,
                        rolls.get(1).getElements(),
                        RandomElementsBuilder.fromRolls(rolls, rollContext),
                        rolls.get(1).getChildrenRolls(),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls));

                return Optional.empty();
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition, arguments);
            }
        };
    }
}

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
                if (arguments.size() < 2) {
                    throw new ExpressionException(String.format("'%s' requires as 2 inputs but was '%s'", getName(), arguments.size()));
                }
                RollContext nameContext = rollContext.copyWithEmptyVariables(); //don't replace literals in the first argument of the function, but it can use new variables
                Optional<List<Roll>> valNameRoll = arguments.getFirst().extendRoll(nameContext);
                if (valNameRoll.isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", expressionPosition.getValue()));
                }
                ImmutableList.Builder<Roll> rollBuilder = ImmutableList.<Roll>builder()
                        .addAll(valNameRoll.get());
                rollContext.merge(nameContext);
                List<RollBuilder> remainingRollBuilder = arguments.subList(1, arguments.size());
                List<Roll> rolls = rollBuilder.addAll(RollBuilder.extendAllBuilder(remainingRollBuilder, rollContext)).build();

                checkRollSize(expressionPosition.getValue(), rolls, getMinArgumentCount(), getMaxArgumentCount());

                String valName = rolls.getFirst().getElements().getFirst().getValue();

                String expression = toExpression();
                rollContext.getVariables().put(valName, new Roll(expression,
                        rolls.get(1).getElements(),
                        RandomElementsBuilder.fromRolls(rolls),
                        rolls.get(1).getChildrenRolls(),
                        maxNumberOfElements, keepChildrenRolls));

                return Optional.empty();
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition.getValue(), arguments);
            }
        };
    }
}

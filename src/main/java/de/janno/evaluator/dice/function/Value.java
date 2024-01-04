package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Value extends Function {
    public Value() {
        super("val", 2);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            if (arguments.size() < 2) {
                throw new ExpressionException(String.format("'%s' requires as 2 inputs but was '%s'", getName(), arguments.size()));
            }
            Map<String, Roll> variableNameMap = new ConcurrentHashMap<>(); //don't replace literals in the first argument of the function, but it can use new variables
            Optional<List<Roll>> valNameRoll = arguments.getFirst().extendRoll(variableNameMap);
            if (valNameRoll.isEmpty()) {
                throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", inputValue));
            }
            ImmutableList.Builder<Roll> rollBuilder = ImmutableList.<Roll>builder()
                    .addAll(valNameRoll.get());
            variables.putAll(variableNameMap);
            List<RollBuilder> remainingRollBuilder = arguments.subList(1, arguments.size());
            List<Roll> rolls = rollBuilder.addAll(RollBuilder.extendAllBuilder(remainingRollBuilder, variables)).build();

            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());

            String valName = rolls.getFirst().getElements().getFirst().getValue();

            String expression = getExpression(inputValue, rolls);
            variables.put(valName, new Roll(expression,
                    rolls.get(1).getElements(),
                    UniqueRandomElements.from(rolls),
                    rolls.get(1).getChildrenRolls()));

            return Optional.empty();
        };
    }
}

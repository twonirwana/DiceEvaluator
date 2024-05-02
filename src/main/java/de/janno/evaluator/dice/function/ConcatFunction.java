package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;

public class ConcatFunction extends Function {
    public ConcatFunction(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("concat", 0, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                if (rolls.isEmpty()) {
                    return Optional.empty();
                }
                String joined = rolls.stream()
                        .map(Roll::getResultString)
                        .collect(Collectors.joining());
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        ImmutableList.of(new RollElement(joined, RollElement.NO_TAG, RollElement.NO_COLOR)),
                        RandomElementsBuilder.fromRolls(rolls),
                        ImmutableList.copyOf(rolls), expressionPosition, maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition, arguments);
            }
        };
    }
}

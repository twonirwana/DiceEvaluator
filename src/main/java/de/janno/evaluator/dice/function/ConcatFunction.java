package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;

public class ConcatFunction extends Function {
    public ConcatFunction() {
        super("concat", 0, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(arguments, variables);
            if (rolls.isEmpty()) {
                return Optional.empty();
            }
            String joined = rolls.stream()
                    .map(Roll::getResultString)
                    .collect(Collectors.joining());
            return Optional.of(ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                    ImmutableList.of(new RollElement(joined, RollElement.NO_TAG, RollElement.NO_COLOR)),
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls))));
        };
    }
}

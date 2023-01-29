package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Min extends Function {
    public Min() {
        super("min", 1, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());

            final RollElement min = rolls.stream()
                    .flatMap(result -> result.getElements().stream())
                    .min(RollElement::compareTo).orElseThrow();

            final ImmutableList<RollElement> res = rolls.stream()
                    .flatMap(result -> result.getElements().stream())
                    .filter(resultElement -> resultElement.compareTo(min) == 0)
                    .collect(ImmutableList.toImmutableList());
            return ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls)));
        };
    }
}

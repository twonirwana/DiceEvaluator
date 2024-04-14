package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Min extends Function {
    public Min(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("min", 1, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                checkRollSize(expressionPosition.value(), rolls, getMinArgumentCount(), getMaxArgumentCount());

                final RollElement min = rolls.stream()
                        .flatMap(result -> result.getElements().stream())
                        .min(RollElement::compareTo).orElseThrow();

                final ImmutableList<RollElement> res = rolls.stream()
                        .flatMap(result -> result.getElements().stream())
                        .filter(resultElement -> resultElement.compareTo(min) == 0)
                        .collect(ImmutableList.toImmutableList());
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.copyOf(rolls),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition.value(), arguments);
            }
        };
    }
}

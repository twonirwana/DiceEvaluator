package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class SortAsc extends Function {

    public SortAsc(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("asc", 1, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                checkRollSize(expressionPosition, rolls, getMinArgumentCount(), getMaxArgumentCount());
                final ImmutableList<RollElement> res = rolls.stream()
                        .flatMap(result -> result.getElements().stream())
                        .sorted()
                        .collect(ImmutableList.toImmutableList());
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
                        RandomElementsBuilder.fromRolls(rolls, rollContext),
                        ImmutableList.copyOf(rolls),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition, arguments);
            }
        };
    }
}

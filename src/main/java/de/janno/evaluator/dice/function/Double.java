package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

/**
 * Deprecated in favor of the mightier 'replace' function.
 */
@Deprecated
public class Double extends Function {

    public Double(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("double", 2, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                checkRollSize(expressionPosition.getValue(), rolls, getMinArgumentCount(), getMaxArgumentCount());
                Roll input = rolls.getFirst();
                Roll toDuplicate = rolls.get(1);

                ImmutableList<RollElement> rollElements = input.getElements().stream()
                        .flatMap(r -> {
                            if (toDuplicate.isElementsContainsElementWithValueAndTag(r)) {
                                return ImmutableList.of(r, r).stream();
                            } else {
                                return ImmutableList.of(r).stream();
                            }
                        })
                        .collect(ImmutableList.toImmutableList());

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        rollElements,
                        RandomElementsBuilder.fromRolls(rolls),
                        ImmutableList.<Roll>builder()
                                .addAll(input.getChildrenRolls())
                                .addAll(toDuplicate.getChildrenRolls())
                                .build(), maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition.getValue(), arguments);
            }
        };

    }
}

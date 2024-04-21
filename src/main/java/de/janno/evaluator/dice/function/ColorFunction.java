package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

/**
 * use tag and color operator
 */
@Deprecated
public class ColorFunction extends Function {

    public ColorFunction(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("color", 2, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {

        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                checkRollSize(expressionPosition.getValue(), rolls, getMinArgumentCount(), getMaxArgumentCount());
                Roll p1 = rolls.getFirst();
                Roll p2 = rolls.get(1);
                checkContainsSingleElement(expressionPosition.getValue(), p2, "second argument");
                String color = p2.getElements().getFirst().getValue();
                RandomElementsBuilder builder = RandomElementsBuilder.empty();
                rolls.forEach(r -> builder.addWithColor(r, color));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        p1.getElements().stream()
                                .map(r -> new RollElement(r.getValue(), color, color))
                                .collect(ImmutableList.toImmutableList()),
                        builder.build(),
                        p1.getChildrenRolls(), maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition.getValue(), arguments);
            }
        };
    }
}

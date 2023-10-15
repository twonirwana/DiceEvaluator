package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

@Deprecated
//"use tag and color operator"
public class ColorFunction extends Function {

    public ColorFunction() {
        super("color", 2);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(arguments, variables);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());
            Roll p1 = rolls.get(0);
            Roll p2 = rolls.get(1);
            checkContainsSingleElement(inputValue, p2, "second argument");
            String color = p2.getElements().get(0).getValue();
            UniqueRandomElements.Builder builder = new UniqueRandomElements.Builder();
            rolls.forEach(r -> builder.addWithColor(r.getRandomElementsInRoll(), color));
            return Optional.of(ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                    p1.getElements().stream()
                            .map(r -> new RollElement(r.getValue(), color, color))
                            .collect(ImmutableList.toImmutableList()),
                    builder.build(),
                    p1.getChildrenRolls())));
        };
    }
}

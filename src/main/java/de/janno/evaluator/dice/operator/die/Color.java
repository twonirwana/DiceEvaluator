package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Color extends Operator {
    public Color() {
        super("col", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(Color.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return variables -> {

            List<Roll> rolls = extendAllBuilder(operands, variables);
            checkRollSize(inputValue, rolls, 2, 2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkAllElementsAreSameTag(inputValue, left, right);
            checkContainsSingleElement(inputValue, right, "second argument");
            String color = right.getElements().get(0).getValue();
            //colors are applied to the random elements, so they can be used for dice images
            UniqueRandomElements.Builder builder = new UniqueRandomElements.Builder();
            rolls.forEach(r -> builder.addWithColor(r.getRandomElementsInRoll(), color));
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    left.getElements().stream()
                            .map(r -> new RollElement(r.getValue(), r.getTag(), color))
                            .collect(ImmutableList.toImmutableList()),
                    builder.build(),
                    ImmutableList.of(left, right)));
        };
    }

}
package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Color extends Operator {
    public Color(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("col", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(Color.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition.value(), rolls, 2, 2);

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);
                checkAllElementsAreSameTag(expressionPosition.value(), left, right);
                checkContainsSingleElement(expressionPosition.value(), right, "second argument");
                String color = right.getElements().getFirst().getValue();
                //colors are applied to the random elements, so they can be used for dice images
                UniqueRandomElements.Builder builder = new UniqueRandomElements.Builder();
                rolls.forEach(r -> builder.addWithColor(r.getRandomElementsInRoll(), color));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        left.getElements().stream()
                                .map(r -> new RollElement(r.getValue(), r.getTag(), color))
                                .collect(ImmutableList.toImmutableList()),
                        builder.build(),
                        ImmutableList.of(left, right),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(getName(), operands);
            }
        };
    }

}
package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Tag extends Operator {
    public Tag() {
        super("tag", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(Tag.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {

            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 2, 2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkAllElementsAreSameTag(inputValue, left, right);
            checkContainsSingleElement(inputValue, right, "second argument");
            String tag = right.getElements().get(0).getValue();

            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    left.getElements().stream()
                            .map(r -> new RollElement(r.getValue(), tag, r.getColor()))
                            .collect(ImmutableList.toImmutableList()),
                    //tags are not applied to the random elements
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }

}
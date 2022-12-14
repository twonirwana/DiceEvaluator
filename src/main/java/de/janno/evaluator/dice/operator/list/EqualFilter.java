package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class EqualFilter extends Operator {

    public EqualFilter() {
        super("==", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(EqualFilter.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(getName(), rolls, 2,2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkContainsSingleElement(getName(), right, "right");

            ImmutableList<RollElement> diceResult = left.getElements().stream()
                    .filter(i -> right.getElements().get(0).equals(i))
                    .collect(ImmutableList.toImmutableList());
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(getName(), rolls),
                    diceResult,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

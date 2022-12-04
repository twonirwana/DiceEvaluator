package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class EqualFilter extends Operator {

    public EqualFilter() {
        super("==", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(EqualFilter.class));
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        Roll left = operands.get(0);
        Roll right = operands.get(1);
        checkContainsSingleElement(getName(), right, "right");

        ImmutableList<RollElement> diceResult = left.getElements().stream()
                .filter(i -> right.getElements().get(0).equals(i))
                .collect(ImmutableList.toImmutableList());
        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                diceResult,
                UniqueRandomElements.from(operands),
                ImmutableList.of(left, right), null
        );
    }
}

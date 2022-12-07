package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class EqualFilter extends Operator {

    public EqualFilter() {
        super("==", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(EqualFilter.class));
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> operands) throws ExpressionException {
        return () -> {
            List<Roll> rolls = rollAllSupplier(operands);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkContainsSingleElement(getName(), right, "right");

            ImmutableList<RollElement> diceResult = left.getElements().stream()
                    .filter(i -> right.getElements().get(0).equals(i))
                    .collect(ImmutableList.toImmutableList());
            return new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    diceResult,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right));
        };
    }
}

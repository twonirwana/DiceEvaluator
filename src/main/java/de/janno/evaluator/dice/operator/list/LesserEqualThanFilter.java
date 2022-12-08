package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;
import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class LesserEqualThanFilter extends Operator {

    public LesserEqualThanFilter() {
        super("<=", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(LesserEqualThanFilter.class));
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = rollAllSupplier(operands, constants);
            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkContainsOnlyInteger(getName(), left, "left");
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            //todo color only filtered by same color?
            ImmutableList<RollElement> diceResult = left.getElements().stream()
                    .filter(i -> i.asInteger().isPresent() && i.asInteger().get() <= rightNumber)
                    .collect(ImmutableList.toImmutableList());
            return new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    diceResult,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right));
        };
    }
}

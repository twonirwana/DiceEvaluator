package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import de.janno.evaluator.dice.operator.RollOperator;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsOnlyInteger;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class LesserThanFilter extends RollOperator {

    public LesserThanFilter() {
        super("<", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(LesserThanFilter.class));
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        Roll left = operands.get(0);
        Roll right = operands.get(1);
        checkContainsOnlyInteger(getName(), left, "left");
        final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        //todo color only filtered by same color?
        ImmutableList<RollElement> diceResult = left.getElements().stream()
                .filter(i -> i.asInteger().isPresent() && i.asInteger().get() < rightNumber)
                .collect(ImmutableList.toImmutableList());
        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                diceResult,
                ImmutableList.<ImmutableList<RollElement>>builder()
                        .addAll(left.getRandomElementsInRoll())
                        .addAll(right.getRandomElementsInRoll())
                        .build(),
                ImmutableList.of(left, right)
        );
    }
}

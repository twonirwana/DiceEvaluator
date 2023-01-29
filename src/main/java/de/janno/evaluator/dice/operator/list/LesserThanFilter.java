package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class LesserThanFilter extends Operator {

    public LesserThanFilter() {
        super("<", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(LesserThanFilter.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 2,2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            checkContainsOnlyInteger(inputValue, left, "left");
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, right, "right"));
            //todo color only filtered by same color?
            ImmutableList<RollElement> diceResult = left.getElements().stream()
                    .filter(i -> i.asInteger().isPresent() && i.asInteger().get() < rightNumber)
                    .collect(ImmutableList.toImmutableList());
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    diceResult,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

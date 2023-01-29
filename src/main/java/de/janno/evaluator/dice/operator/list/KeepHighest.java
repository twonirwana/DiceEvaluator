package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepHighest extends Operator {

    public KeepHighest() {
        super("k", null, null, Operator.Associativity.LEFT, getOderNumberOf(KeepHighest.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 2, 2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, right, "right"));
            if (rightNumber < 0) {
                throw new ExpressionException(String.format("The number to keep can not be negativ but was %d", rightNumber));
            }
            //todo right color only filtered by same color?
            ImmutableList<RollElement> keep = left.getElements().stream()
                    .collect(Collectors.groupingBy(RollElement::getColor)).values().stream()
                    .flatMap(cl -> cl.stream()
                            .sorted(Comparator.reverseOrder())
                            .limit(rightNumber)
                    )
                    .collect(ImmutableList.toImmutableList());
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(inputValue, rolls),
                    keep,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

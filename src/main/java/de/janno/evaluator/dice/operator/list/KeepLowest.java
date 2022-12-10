package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepLowest extends Operator {

    public KeepLowest() {
        super(Set.of("l", "L"), null, null, Operator.Associativity.LEFT, getOderNumberOf(KeepLowest.class));
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(getName(), rolls, 2,2);

            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            //todo right color only filtered by same color?
            ImmutableList<RollElement> keep = left.getElements().stream()
                    .collect(Collectors.groupingBy(RollElement::getColor)).values().stream()
                    .flatMap(cl -> cl.stream()
                            .sorted()
                            .limit(rightNumber)
                    )
                    .collect(ImmutableList.toImmutableList());
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    keep,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right)));
        };
    }
}

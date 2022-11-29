package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Operator;
import de.janno.evaluator.dice.RandomElement;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepLowest extends Operator {

    public KeepLowest() {
        super(Set.of("l", "L"), null, null, Operator.Associativity.LEFT, getOderNumberOf(KeepLowest.class));
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {
        Roll left = operands.get(0);
        Roll right = operands.get(1);
        final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
        //todo right color only filtered by same color?
        ImmutableList<RollElement> keep = left.getElements().stream()
                .collect(Collectors.groupingBy(RollElement::getColor)).values().stream()
                .flatMap(cl -> cl.stream()
                        .sorted()
                        .limit(rightNumber)
                )
                .collect(ImmutableList.toImmutableList());
        return new Roll(getBinaryOperatorExpression(getPrimaryName(), operands),
                keep,
                ImmutableList.<ImmutableList<RandomElement>>builder()
                        .addAll(left.getRandomElementsInRoll())
                        .addAll(right.getRandomElementsInRoll())
                        .build(),
                ImmutableList.of(left, right), null);
    }
}

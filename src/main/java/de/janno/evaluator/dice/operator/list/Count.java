package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Operator;
import de.janno.evaluator.dice.Roll;
import de.janno.evaluator.dice.RollElement;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Count extends Operator {

    public Count() {
        super(Set.of("c", "C"), Operator.Associativity.LEFT, getOderNumberOf(Count.class), null, null);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> operands) throws ExpressionException {

        Roll left = operands.get(0);
        //count of each color separate
        ImmutableList<RollElement> res;
        if (operands.stream().mapToLong(result -> result.getElements().size()).sum() == 0) {
            res = ImmutableList.of(new RollElement("0", RollElement.NO_COLOR));
        } else {
            res = left.getElements().stream()
                    .collect(Collectors.groupingBy(RollElement::getColor)).entrySet().stream()
                    .map(e -> new RollElement(String.valueOf(e.getValue().size()), e.getKey()))
                    .collect(ImmutableList.toImmutableList());
        }
        return new Roll(getLeftUnaryExpression(getPrimaryName(), operands),
                res,
                left.getRandomElementsInRoll(),
                ImmutableList.of(left)
        );
    }
}

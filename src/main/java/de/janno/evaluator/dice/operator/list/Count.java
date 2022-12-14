package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Count extends Operator {

    public Count() {
        super("c", Operator.Associativity.LEFT, getOderNumberOf(Count.class), null, null);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(getName(), rolls, 1, 1);

            Roll left = rolls.get(0);

            //count of each color separate
            ImmutableList<RollElement> res;
            if (rolls.stream().mapToLong(result -> result.getElements().size()).sum() == 0) {
                res = ImmutableList.of(new RollElement("0", RollElement.NO_COLOR));
            } else {
                res = left.getElements().stream()
                        .collect(Collectors.groupingBy(RollElement::getColor)).entrySet().stream()
                        .map(e -> new RollElement(String.valueOf(e.getValue().size()), e.getKey()))
                        .collect(ImmutableList.toImmutableList());
            }
            return ImmutableList.of(new Roll(getLeftUnaryExpression(getName(), rolls),
                    res,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left)));
        };
    }

}

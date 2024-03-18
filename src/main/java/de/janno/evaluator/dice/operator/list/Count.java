package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Count extends Operator {

    public Count(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("c", OperatorType.UNARY, Operator.Associativity.LEFT, getOderNumberOf(Count.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variables) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, variables);
                checkRollSize(inputValue, rolls, 1, 1);

                Roll left = rolls.getFirst();

                //count of each tag separate
                ImmutableList<RollElement> res;
                if (rolls.stream().mapToLong(result -> result.getElements().size()).sum() == 0) {
                    res = ImmutableList.of(new RollElement("0", RollElement.NO_TAG, RollElement.NO_COLOR));
                } else {
                    res = left.getElements().stream()
                            .collect(Collectors.groupingBy(RollElement::getTag)).entrySet().stream()
                            .map(e -> new RollElement(String.valueOf(e.getValue().size()), e.getKey(), RollElement.NO_COLOR))
                            .collect(ImmutableList.toImmutableList());
                }
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(left),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getLeftUnaryExpression(inputValue, operands);
            }
        };
    }

}

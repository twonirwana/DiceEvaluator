package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
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
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition, rolls, 1, 1);

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
                        RandomElementsBuilder.fromRolls(rolls, rollContext),
                        ImmutableList.of(left),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getLeftUnaryExpression(expressionPosition, operands);
            }
        };
    }

}

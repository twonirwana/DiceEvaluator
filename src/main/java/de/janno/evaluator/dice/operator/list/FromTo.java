package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class FromTo extends Operator {

    public FromTo(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("...", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(FromTo.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) {

        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition, rolls, 2, 2);

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);
                checkAllElementsAreSameTag(expressionPosition, left, right);
                final int leftNumber = left.asInteger().orElseThrow(() -> throwNotDecimalExpression(expressionPosition, left, "left"));
                final int rightNumber = right.asInteger().orElseThrow(() -> throwNotDecimalExpression(expressionPosition, right, "right"));

                if (leftNumber > rightNumber) {
                    throw new ExpressionException("The left number must be smaller or equal than the right number", expressionPosition);
                }

                if (rightNumber - leftNumber + 1 > 100) {
                    throw new ExpressionException("The list is limited to 100 elements", expressionPosition);
                }

                final ImmutableList<RollElement> numbers = IntStream.range(leftNumber, rightNumber + 1)
                        .boxed()
                        .map(i -> new RollElement(String.valueOf(i), RollElement.NO_TAG, RollElement.NO_COLOR))
                        .collect(ImmutableList.toImmutableList());
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        numbers,
                        RandomElementsBuilder.fromRolls(rolls, rollContext),
                        ImmutableList.copyOf(rolls),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(expressionPosition, operands);
            }
        };
    }
}

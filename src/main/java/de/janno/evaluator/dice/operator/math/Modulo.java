package de.janno.evaluator.dice.operator.math;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class Modulo extends Operator {

    public Modulo(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("mod", OperatorType.BINARY, Associativity.LEFT, getOderNumberOf(Modulo.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition.value(), rolls, 2, 2);

                Roll left = rolls.getFirst();
                Roll right = rolls.get(1);
                checkAllElementsAreSameTag(expressionPosition.value(), left, right);
                final int leftNumber = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.value(), left, "left"));
                final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.value(), right, "right"));

                final ImmutableList<RollElement> res = ImmutableList.of(new RollElement(String.valueOf(leftNumber % rightNumber), left.getElements().getFirst().getTag(), RollElement.NO_COLOR));
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        res,
                        UniqueRandomElements.from(rolls),
                        ImmutableList.of(left, right),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(expressionPosition.value(), operands);
            }
        };
    }
}

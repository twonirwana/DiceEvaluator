package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Concat extends Operator {
    public Concat(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("_", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Concat.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) {

        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                String joined = rolls.stream()
                        .map(Roll::getResultString)
                        .collect(Collectors.joining());
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        ImmutableList.of(new RollElement(joined, RollElement.NO_TAG, RollElement.NO_COLOR)),
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

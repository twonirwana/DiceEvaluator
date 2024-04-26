package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class Reroll extends Operator {
    public Reroll(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("rr", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, getOderNumberOf(Reroll.class), maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                RollBuilder inputBuilder = operands.getFirst();
                List<Roll> compareTos = operands.get(1).extendRoll(rollContext).orElse(Collections.emptyList());
                checkRollSize(expressionPosition, compareTos, 1, 1);
                Roll compareTo = compareTos.getFirst();

                List<Roll> rolls = inputBuilder.extendRoll(rollContext).orElse(Collections.emptyList());
                checkRollSize(expressionPosition, rolls, 1, 1);
                Roll roll = rolls.getFirst();
                RandomElementsBuilder builder = RandomElementsBuilder.ofRoll(roll).addRoll(compareTo);

                if (roll.getElements().stream().anyMatch(compareTo::isElementsContainsElementWithValueAndTag)) {
                    rolls = inputBuilder.extendRoll(rollContext).orElse(Collections.emptyList());
                    checkRollSize(expressionPosition, rolls, 1, 1);
                    roll = rolls.getFirst();
                    builder.addRoll(roll);
                }

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        roll.getElements(),
                        builder.build(),
                        ImmutableList.<Roll>builder()
                                .addAll(compareTo.getChildrenRolls())
                                .addAll(roll.getChildrenRolls())
                                .build(),
                        maxNumberOfElements,
                        keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getBinaryOperatorExpression(expressionPosition, operands);
            }
        };
    }

}
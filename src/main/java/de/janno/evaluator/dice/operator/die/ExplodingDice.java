package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.DiceHelper.explodingDice;
import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class ExplodingDice extends Operator {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public ExplodingDice(NumberSupplier numberSupplier, int maxNumberOfDice, int maxNumberOfElements, boolean keepChildrenRolls) {
        super("d!", Operator.Associativity.RIGHT, getOderNumberOf(ExplodingDice.class), Operator.Associativity.LEFT, getOderNumberOf(ExplodingDice.class), maxNumberOfElements, keepChildrenRolls);
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {

            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, rollContext);
                checkRollSize(expressionPosition.getValue(), rolls, 1, 2);

                RandomElementsBuilder randomElements = RandomElementsBuilder.empty();

                final RollId rollId = RollId.of(expressionPosition, rollContext.getNextReEvaluationNumber(expressionPosition));

                final int numberOfDice;
                final int sidesOfDie;
                final ImmutableList<Roll> childrenRolls;
                if (rolls.size() == 1) {
                    numberOfDice = 1;
                    final Roll right = rolls.getFirst();
                    sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.getValue(), right, "right"));
                    childrenRolls = ImmutableList.of(right);
                    randomElements.addRoll(right);
                } else {
                    final Roll left = rolls.getFirst();
                    final Roll right = rolls.get(1);
                    randomElements.addRoll(left);
                    randomElements.addRoll(right);
                    numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.getValue(), left, "left"));
                    sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.getValue(), right, "right"));
                    childrenRolls = ImmutableList.of(left, right);
                }

                if (numberOfDice > maxNumberOfDice) {
                    throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
                }
                if (numberOfDice < 0) {
                    throw new ExpressionException(String.format("The number of dice can not be negativ but was %d", numberOfDice));
                }
                if (sidesOfDie < 2) {
                    throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
                }

                final ImmutableList<RandomElement> roll = explodingDice(numberOfDice, sidesOfDie, numberSupplier, rollId);
                final ImmutableList<RollElement> rollElements = roll.stream().map(RandomElement::getRollElement).collect(ImmutableList.toImmutableList());

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        rollElements,
                        randomElements
                                .addRandomElements(roll)
                                .build(),
                        childrenRolls,
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                if (operands.size() == 1) {
                    return getRightUnaryExpression(expressionPosition.getValue(), operands);
                }
                return getBinaryOperatorExpression(expressionPosition.getValue(), operands);
            }
        };
    }
}

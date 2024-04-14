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
                checkRollSize(expressionPosition.value(), rolls, 1, 2);

                UniqueRandomElements.Builder randomElements = UniqueRandomElements.builder();
                final RollId rollId = RollId.of(expressionPosition, rollContext.getNextReEvaluationNumber(expressionPosition));

                //todo combine rolls size 1 and more than 1
                if (rolls.size() == 1) {
                    final Roll right = rolls.getFirst();
                    final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.value(), right, "right"));
                    if (sidesOfDie < 2) {
                        throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
                    }
                    final ImmutableList<RandomElement> roll = explodingDice(1, sidesOfDie, numberSupplier, rollId);
                    final ImmutableList<RollElement> rollElements = roll.stream().map(RandomElement::getRollElement).collect(ImmutableList.toImmutableList());
                    randomElements.addAsRandomElements(roll, rollId);

                    return Optional.of(ImmutableList.of(new Roll(toExpression(),
                            rollElements,
                            randomElements.build(),
                            ImmutableList.of(right),
                            maxNumberOfElements, keepChildrenRolls)));
                }

                final Roll left = rolls.getFirst();
                final Roll right = rolls.get(1);
                randomElements.add(left.getRandomElementsInRoll());
                randomElements.add(right.getRandomElementsInRoll());

                final int numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.value(), left, "left"));
                if (numberOfDice > maxNumberOfDice) {
                    throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
                }
                if (numberOfDice < 0) {
                    throw new ExpressionException(String.format("The number of dice can not be negativ but was %d", numberOfDice));
                }
                final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.value(), right, "right"));
                if (sidesOfDie < 2) {
                    throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
                }

                final ImmutableList<RandomElement> roll = explodingDice(numberOfDice, sidesOfDie, numberSupplier, rollId);
                final ImmutableList<RollElement> rollElements = roll.stream().map(RandomElement::getRollElement).collect(ImmutableList.toImmutableList());
                randomElements.addAsRandomElements(roll, rollId);

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        rollElements,
                        randomElements.build(),
                        ImmutableList.of(left, right),
                        maxNumberOfElements, keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                if (operands.size() == 1) {
                    return getRightUnaryExpression(expressionPosition.value(), operands);
                }
                return getBinaryOperatorExpression(expressionPosition.value(), operands);
            }
        };
    }
}

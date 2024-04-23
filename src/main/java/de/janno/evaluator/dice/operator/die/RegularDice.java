package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.DiceHelper.pickOneOf;
import static de.janno.evaluator.dice.DiceHelper.rollDice;
import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class RegularDice extends Operator {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public RegularDice(NumberSupplier numberSupplier, int maxNumberOfDice, int maxNumberOfElements, boolean keepChildrenRolls) {
        super("d", Operator.Associativity.RIGHT, getOderNumberOf(RegularDice.class), Operator.Associativity.LEFT, getOderNumberOf(RegularDice.class), maxNumberOfElements, keepChildrenRolls);
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

                final int numberOfDice;
                final Roll right;
                final ImmutableList<Roll> childrenRolls;
                final String expression;
                final RollId rollId = RollId.of(expressionPosition, rollContext.getNextReEvaluationNumber(expressionPosition));

                RandomElementsBuilder randomElements = RandomElementsBuilder.empty();
                if (rolls.size() == 1) {
                    right = rolls.getFirst();
                    randomElements.addRoll(right);
                    numberOfDice = 1;
                    childrenRolls = ImmutableList.of(right);
                    expression = toExpression();
                } else if (rolls.size() == 2) {
                    Roll left = rolls.getFirst();
                    right = rolls.get(1);
                    childrenRolls = ImmutableList.of(left, right);
                    numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.getValue(), left, "left"));
                    expression = toExpression();
                    randomElements.addRoll(left);
                    randomElements.addRoll(right);

                } else {
                    throw new IllegalStateException("More then two operands for " + expressionPosition.getValue());
                }

                if (numberOfDice > maxNumberOfDice) {
                    throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
                }
                if (numberOfDice < 0) {
                    throw new ExpressionException(String.format("The number of dice can not be negativ but was %d", numberOfDice));
                }
                final ImmutableList<RollElement> rollElements;
                if (right.asInteger().isPresent()) {
                    int sidesOfDie = right.asInteger().get();
                    List<RandomElement> roll = rollDice(numberOfDice, sidesOfDie, numberSupplier, rollId);
                    rollElements = roll.stream().map(RandomElement::getRollElement).collect(ImmutableList.toImmutableList());
                    randomElements.addRandomElements(roll);
                } else {
                    ImmutableList.Builder<RandomElement> rollBuilder = ImmutableList.builder();
                    for (int i = 0; i < numberOfDice; i++) {
                        rollBuilder.add(pickOneOf(right.getElements(), numberSupplier, DieId.of(rollId, i, 0)));
                    }
                    List<RandomElement> roll = rollBuilder.build();
                    rollElements = roll.stream().map(RandomElement::getRollElement).collect(ImmutableList.toImmutableList());
                    randomElements.addRandomElements(roll);
                }

                return Optional.of(ImmutableList.of(new Roll(expression,
                        rollElements,
                        randomElements.build(),
                        childrenRolls,
                        maxNumberOfElements,
                        keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                if (operands.size() == 1) {
                    return getRightUnaryExpression(expressionPosition, operands);
                }
                return getBinaryOperatorExpression(expressionPosition, operands);
            }
        };
    }
}

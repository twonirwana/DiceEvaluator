package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.DiceHelper.*;
import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public final class RegularDice extends Operator {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public RegularDice(NumberSupplier numberSupplier, int maxNumberOfDice) {
        super("d", Operator.Associativity.RIGHT, getOderNumberOf(RegularDice.class), Operator.Associativity.LEFT, getOderNumberOf(RegularDice.class));
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(inputValue, rolls, 1, 2);

            final int numberOfDice;
            final Roll right;
            final ImmutableList<Roll> childrenRolls;
            final String expression;
            UniqueRandomElements.Builder randomElements = UniqueRandomElements.builder();
            if (rolls.size() == 1) {
                right = rolls.get(0);
                numberOfDice = 1;
                childrenRolls = ImmutableList.of(right);
                expression = getRightUnaryExpression(inputValue, rolls);
            } else if (rolls.size() == 2) {
                Roll left = rolls.get(0);
                right = rolls.get(1);
                childrenRolls = ImmutableList.of(left, right);
                numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, left, "left"));
                expression = getBinaryOperatorExpression(inputValue, rolls);
                randomElements.add(left.getRandomElementsInRoll());

            } else {
                throw new IllegalStateException("More then two operands for " + inputValue);
            }
            randomElements.add(right.getRandomElementsInRoll());

            if (numberOfDice > maxNumberOfDice) {
                throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
            }
            if (numberOfDice < 0) {
                throw new ExpressionException(String.format("The number of dice can not be negativ but was %d", numberOfDice));
            }
            final ImmutableList<RollElement> rollElements;
            if (right.asInteger().isPresent()) {
                int sidesOfDie = right.asInteger().get();
                rollElements = toRollElements(rollDice(numberOfDice, sidesOfDie, numberSupplier));

                randomElements.addAsRandomElements(rollElements.stream()
                        .map(r -> new RandomElement(r, 1, sidesOfDie))
                        .collect(ImmutableList.toImmutableList()));
            } else {
                ImmutableList.Builder<RollElement> builder = ImmutableList.builder();
                for (int i = 0; i < numberOfDice; i++) {
                    builder.add(pickOneOf(right.getElements(), numberSupplier));
                }
                rollElements = builder.build();
                randomElements.addAsRandomElements(rollElements.stream()
                        .map(r -> new RandomElement(r, right.getElements().stream()
                                .map(RollElement::getValue)
                                .collect(ImmutableList.toImmutableList())))
                        .collect(ImmutableList.toImmutableList()));
            }

            return ImmutableList.of(new Roll(expression,
                    rollElements,
                    randomElements.build(),
                    childrenRolls));
        };
    }
}

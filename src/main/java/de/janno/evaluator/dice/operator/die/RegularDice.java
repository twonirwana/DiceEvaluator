package de.janno.evaluator.dice.operator.die;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.janno.evaluator.dice.DiceHelper.*;
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
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull String inputValue) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull Map<String, Roll> variables) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(operands, variables);
                checkRollSize(inputValue, rolls, 1, 2);

                final int numberOfDice;
                final Roll right;
                final ImmutableList<Roll> childrenRolls;
                final String expression;
                UniqueRandomElements.Builder randomElements = UniqueRandomElements.builder();
                if (rolls.size() == 1) {
                    right = rolls.getFirst();
                    numberOfDice = 1;
                    childrenRolls = ImmutableList.of(right);
                    expression = toExpression();
                } else if (rolls.size() == 2) {
                    Roll left = rolls.getFirst();
                    right = rolls.get(1);
                    childrenRolls = ImmutableList.of(left, right);
                    numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(inputValue, left, "left"));
                    expression = toExpression();
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
                    return getRightUnaryExpression(inputValue, operands);
                }
                return getBinaryOperatorExpression(inputValue, operands);
            }
        };
    }
}

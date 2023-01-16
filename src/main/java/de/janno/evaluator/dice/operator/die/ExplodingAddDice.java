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

public final class ExplodingAddDice extends Operator {
    private final NumberSupplier numberSupplier;
    private final int maxNumberOfDice;

    public ExplodingAddDice(NumberSupplier numberSupplier, int maxNumberOfDice) {
        super("d!!", Operator.Associativity.RIGHT, getOderNumberOf(ExplodingAddDice.class), Operator.Associativity.LEFT, getOderNumberOf(ExplodingAddDice.class));
        this.numberSupplier = numberSupplier;
        this.maxNumberOfDice = maxNumberOfDice;
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(operands, constants);
            checkRollSize(getName(), rolls, 1, 2);

            UniqueRandomElements.Builder randomElements = UniqueRandomElements.builder();
            if (rolls.size() == 1) {
                final Roll right = rolls.get(0);
                final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
                if (sidesOfDie < 2) {
                    throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
                }
                final ImmutableList<ExplodedAddDie> explodingAddDice = explodingAddDice(1, sidesOfDie, numberSupplier);
                final ImmutableList<RollElement> rollElements = explodedAddDie2RollElements(explodingAddDice);
                randomElements.add(right.getRandomElementsInRoll());
                randomElements.addAsRandomElements(explodedAddDie2RandomElements(explodingAddDice));
                return ImmutableList.of(new Roll(getRightUnaryExpression(getName(), rolls),
                        rollElements,
                        randomElements.build(),
                        ImmutableList.of(right)));
            }

            final Roll left = rolls.get(0);
            final Roll right = rolls.get(1);
            randomElements.add(left.getRandomElementsInRoll());
            randomElements.add(right.getRandomElementsInRoll());
            final int numberOfDice = left.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), left, "left"));
            if (numberOfDice > maxNumberOfDice) {
                throw new ExpressionException(String.format("The number of dice must be less or equal then %d but was %d", maxNumberOfDice, numberOfDice));
            }
            if (numberOfDice < 0) {
                throw new ExpressionException(String.format("The number of dice can not be negativ but was %d", numberOfDice));
            }
            final int sidesOfDie = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            if (sidesOfDie < 2) {
                throw new ExpressionException(String.format("The number of sides of a die must be greater then 1 but was %d", sidesOfDie));
            }
            final ImmutableList<ExplodedAddDie> explodingAddDice = explodingAddDice(numberOfDice, sidesOfDie, numberSupplier);
            final ImmutableList<RollElement> rollElements = explodedAddDie2RollElements(explodingAddDice);
            randomElements.addAsRandomElements(explodedAddDie2RandomElements(explodingAddDice));
            return ImmutableList.of(new Roll(getBinaryOperatorExpression(getName(), rolls),
                    rollElements,
                    randomElements.build(),
                    ImmutableList.of(left, right)));
        };
    }
}

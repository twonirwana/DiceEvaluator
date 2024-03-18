package de.janno.evaluator.dice.function;

import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Roll;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;

public class IfGreater extends AbstractIf {
    public IfGreater(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("ifG", maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    protected boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) throws ExpressionException {
        checkContainsSingleElement(getName(), input, "%d argument".formatted(inputPosition));
        checkContainsSingleElement(getName(), compareTo, "%d argument".formatted(compareToPosition));
        return input.getElements().getFirst().compareTo(compareTo.getElements().getFirst()) > 0;
    }

}
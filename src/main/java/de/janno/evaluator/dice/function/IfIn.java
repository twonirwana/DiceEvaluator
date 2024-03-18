package de.janno.evaluator.dice.function;

import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.Roll;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;

public class IfIn extends AbstractIf {
    public IfIn(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("ifIn", maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    protected boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) throws ExpressionException {
        checkContainsSingleElement(getName(), input, "%d argument".formatted(inputPosition));
        return compareTo.isElementsContainsElementWithValueAndTag(input.getElements().getFirst());
    }
}
package de.janno.evaluator.dice.function;

import de.janno.evaluator.ExpressionException;
import de.janno.evaluator.dice.Roll;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;

public class IfLesser extends AbstractIf {
    public IfLesser() {
        super("ifL");
    }

    @Override
    protected boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) throws ExpressionException {
        checkContainsSingleElement(getName(), input, "%d argument".formatted(inputPosition));
        checkContainsSingleElement(getName(), compareTo, "%d argument".formatted(compareToPosition));
        return input.getElements().get(0).compareTo(compareTo.getElements().get(0)) < 0;
    }
}
package de.janno.evaluator.dice.function;

import de.janno.evaluator.dice.ExpressionException;
import de.janno.evaluator.dice.ExpressionPosition;
import de.janno.evaluator.dice.Roll;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;

public class IfLesser extends AbstractIf {
    public IfLesser(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("ifL", maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    protected boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition, ExpressionPosition expressionPosition) throws ExpressionException {
        checkContainsSingleElement(expressionPosition, input, "%d argument".formatted(inputPosition));
        checkContainsSingleElement(expressionPosition, compareTo, "%d argument".formatted(compareToPosition));
        return input.getElements().getFirst().compareTo(compareTo.getElements().getFirst()) < 0;
    }
}
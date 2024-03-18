package de.janno.evaluator.dice.function;

import de.janno.evaluator.dice.Roll;

public class IfEqual extends AbstractIf {
    public IfEqual(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("ifE", maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    protected boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) {
        return input.equalForValueAndTag(compareTo);
    }
}
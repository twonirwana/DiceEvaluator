package de.janno.evaluator.dice.function;

import de.janno.evaluator.dice.Roll;

public class IfEqual extends AbstractIf {
    public IfEqual() {
        super("ifE");
    }

    @Override
    protected boolean compare(Roll input, int inputPosition, Roll compareTo, int compareToPosition) {
        return input.getElements().equals(compareTo.getElements());
    }
}
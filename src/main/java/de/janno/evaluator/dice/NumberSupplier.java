package de.janno.evaluator.dice;

public interface NumberSupplier {
    int get(int minExcl, int maxIncl);
}

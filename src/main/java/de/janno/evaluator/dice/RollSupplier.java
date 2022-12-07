package de.janno.evaluator.dice;

import java.util.Map;

@FunctionalInterface
public interface RollSupplier {
    Roll roll() throws ExpressionException;



    default Map<String, Roll> getConstant() throws ExpressionException {
        return null;
    }

    default boolean isConstant(){
        return false;
    }
}

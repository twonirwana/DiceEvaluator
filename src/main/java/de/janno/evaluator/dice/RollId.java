package de.janno.evaluator.dice;

import lombok.NonNull;

public record RollId(@NonNull ExpressionPosition expressionPosition, int reevaluate) {
    public static RollId of(ExpressionPosition expressionPosition, int reevaluate) {
        return new RollId(expressionPosition, reevaluate);
    }

    @Override
    public String toString(){
        return expressionPosition + "e" + reevaluate;
    }
}

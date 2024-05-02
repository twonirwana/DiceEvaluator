package de.janno.evaluator.dice;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ExpressionException extends Exception {

    @NonNull
    private final ExpressionPosition expressionPosition;

    public ExpressionException(@NonNull String message, @NonNull ExpressionPosition expressionPosition) {
        super(message);
        this.expressionPosition = expressionPosition;
    }
}

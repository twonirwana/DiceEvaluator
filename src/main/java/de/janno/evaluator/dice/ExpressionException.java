package de.janno.evaluator.dice;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class ExpressionException extends Exception {

    @Nullable
    private final ExpressionPosition expressionPosition;

    public ExpressionException(String message, @Nullable ExpressionPosition expressionPosition) {
        super(message);
        this.expressionPosition = expressionPosition;
    }
}

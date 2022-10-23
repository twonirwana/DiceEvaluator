package de.janno.evaluator.dice.operator;

import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Roll;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

public abstract class RollOperator extends Operator<Roll> {
    public RollOperator(@NonNull String name, OperatorType operatorType, @NonNull Associativity associativity, int precedence) {
        super(name, operatorType, associativity, precedence);
    }

    public RollOperator(@NonNull String name, Associativity unaryAssociativity, Integer unaryPrecedence, Associativity binaryAssociativity, Integer binaryPrecedence) {
        super(name, unaryAssociativity, unaryPrecedence, binaryAssociativity, binaryPrecedence);
    }

    public RollOperator(@NonNull Set<String> names, Associativity unaryAssociativity, Integer unaryPrecedence, Associativity binaryAssociativity, Integer binaryPrecedence) {
        super(names, unaryAssociativity, unaryPrecedence, binaryAssociativity, binaryPrecedence);
    }

    protected static String getBinaryOperatorExpression(String name, List<Roll> operands) {
        return String.format("%s%s%s", operands.get(0).getExpression(), name, operands.get(1).getExpression());
    }

    protected static String getLeftUnaryExpression(String name, List<Roll> operands) {
        return String.format("%s%s", operands.get(0).getExpression(), name);
    }

    protected static String getRightUnaryExpression(String name, List<Roll> operands) {
        return String.format("%s%s", name, operands.get(0).getExpression());
    }
}

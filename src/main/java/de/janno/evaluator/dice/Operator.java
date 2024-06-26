package de.janno.evaluator.dice;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
@ToString
public abstract class Operator {
    protected final int maxNumberOfElements;
    protected final boolean keepChildrenRolls;
    @NonNull
    private final String name;
    private final Integer unaryPrecedence;
    private final Integer binaryPrecedence;

    private final Associativity unaryAssociativity;
    private final Associativity binaryAssociativity;

    public Operator(@NonNull String name, OperatorType operatorType, @NonNull Associativity associativity, int precedence, int maxNumberOfElements, boolean keepChildrenRolls) {
        this(name, getUnaryAssociativity(operatorType, associativity),
                getUnaryPrecedence(operatorType, precedence),
                getBinaryAssociativity(operatorType, associativity),
                getBinaryPrecedence(operatorType, precedence),
                maxNumberOfElements,
                keepChildrenRolls);
    }

    /**
     * The <a href="http://en.wikipedia.org/wiki/Order_of_operations">precedence</a> is the priority of the operator.
     * An operator with a higher precedence will be executed before an operator with a lower precedence.
     * Example : In "1+3*4" * has a higher precedence than +, so the expression is interpreted as 1+(3*4).
     * An operator's <a href="http://en.wikipedia.org/wiki/Operator_associativity">associativity</a> define how operators of the same precedence are grouped.
     */
    public Operator(@NonNull String name, Associativity unaryAssociativity, Integer unaryPrecedence, Associativity binaryAssociativity, Integer binaryPrecedence, int maxNumberOfElements, boolean keepChildrenRolls) {
        if (unaryAssociativity == null && binaryAssociativity == null) {
            throw new IllegalArgumentException("The operant %s need at least on associativity".formatted(name));
        }
        this.name = name;
        this.unaryAssociativity = unaryAssociativity;
        this.binaryAssociativity = binaryAssociativity;
        this.unaryPrecedence = unaryPrecedence;
        this.binaryPrecedence = binaryPrecedence;
        this.maxNumberOfElements = maxNumberOfElements;
        this.keepChildrenRolls = keepChildrenRolls;
    }

    private static Associativity getUnaryAssociativity(OperatorType operatorType, @NonNull Associativity associativity) {
        if (operatorType == OperatorType.UNARY) {
            return associativity;
        }
        return null;
    }

    private static Associativity getBinaryAssociativity(OperatorType operatorType, @NonNull Associativity associativity) {
        if (operatorType == OperatorType.BINARY) {
            return associativity;
        }
        return null;
    }

    private static Integer getUnaryPrecedence(OperatorType operatorType, int precedence) {
        if (operatorType == OperatorType.UNARY) {
            return precedence;
        }
        return null;
    }

    private static Integer getBinaryPrecedence(OperatorType operatorType, int precedence) {
        if (operatorType == OperatorType.BINARY) {
            return precedence;
        }
        return null;
    }

    private static <T> Optional<T> getIndexIfExists(List<T> list, int index) {
        if (list.size() <= index) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index));
    }

    protected static String getBinaryOperatorExpression(ExpressionPosition expressionPosition, List<RollBuilder> operands) {
        String left = getIndexIfExists(operands, 0).map(RollBuilder::toExpression).orElse("");
        String right = getIndexIfExists(operands, 1).map(RollBuilder::toExpression).orElse("");
        return String.format("%s%s%s", left, expressionPosition.toStringWithExtension(), right);
    }

    protected static String getLeftUnaryExpression(ExpressionPosition expressionPosition, List<RollBuilder> operands) {
        String left = getIndexIfExists(operands, 0).map(RollBuilder::toExpression).orElse("");
        return String.format("%s%s", left, expressionPosition.toStringWithExtension());
    }

    protected static String getRightUnaryExpression(ExpressionPosition expressionPosition, List<RollBuilder> operands) {
        String right = getIndexIfExists(operands, 0).map(RollBuilder::toExpression).orElse("");
        return String.format("%s%s", expressionPosition.toStringWithExtension(), right);
    }

    /**
     * Creates a RollBuilder for the arguments
     *
     * @param operands           1 or 2 operants
     * @param expressionPosition the part of the expression for this operant, is needed the get the used upper/lower case in the result expression
     * @return the RollBuilder that can be called to get result rolls
     */
    public abstract @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands, @NonNull ExpressionPosition expressionPosition) throws ExpressionException;

    public boolean supportUnaryOperation() {
        return unaryAssociativity != null;
    }

    public boolean supportBinaryOperation() {
        return binaryAssociativity != null;
    }

    public @NonNull String getName() {
        return name;
    }

    public Associativity getAssociativityForOperantType(OperatorType operatorType) {
        if (operatorType == OperatorType.UNARY && unaryAssociativity != null) {
            return unaryAssociativity;
        }
        if (operatorType == OperatorType.BINARY && binaryAssociativity != null) {
            return binaryAssociativity;
        }
        return null;
    }

    public int getPrecedenceForOperantType(OperatorType operatorType) {
        if (operatorType == OperatorType.UNARY && unaryPrecedence != null) {
            return unaryPrecedence;
        }
        if (operatorType == OperatorType.BINARY && binaryPrecedence != null) {
            return binaryPrecedence;
        }
        throw new IllegalStateException("'%s' has no precedence for a operand type of %s".formatted(getName(), operatorType));
    }

    public enum Associativity {
        LEFT,
        RIGHT
    }

    public enum OperatorType {
        UNARY(1),
        BINARY(2);

        public final int argumentCount;

        OperatorType(int argumentCount) {
            this.argumentCount = argumentCount;
        }

        public static OperatorType of(int argumentCount) {
            if (argumentCount == 1) {
                return UNARY;
            }
            if (argumentCount == 2) {
                return BINARY;
            }
            throw new IllegalArgumentException("OperatorType must have a argumentCount of 1 or 2");
        }
    }
}
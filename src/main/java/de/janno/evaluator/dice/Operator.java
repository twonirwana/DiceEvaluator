package de.janno.evaluator.dice;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode
@ToString
public abstract class Operator {
    @NonNull
    private final ImmutableSet<String> names;
    private final Integer unaryPrecedence;
    private final Integer binaryPrecedence;

    private final Associativity unaryAssociativity;
    private final Associativity binaryAssociativity;

    public Operator(@NonNull String name, OperatorType operatorType, @NonNull Associativity associativity, int precedence) {
        this(name, getUnaryAssociativity(operatorType, associativity),
                getUnaryPrecedence(operatorType, precedence),
                getBinaryAssociativity(operatorType, associativity),
                getBinaryPrecedence(operatorType, precedence));
    }

    public Operator(@NonNull String name, Associativity unaryAssociativity, Integer unaryPrecedence, Associativity binaryAssociativity, Integer binaryPrecedence) {
        this(ImmutableSet.of(name), unaryAssociativity, unaryPrecedence, binaryAssociativity, binaryPrecedence);
    }

    /**
     * The <a href="http://en.wikipedia.org/wiki/Order_of_operations">precedence</a> is the priority of the operator.
     * An operator with a higher precedence will be executed before an operator with a lower precedence.
     * Example : In "1+3*4" * has a higher precedence than +, so the expression is interpreted as 1+(3*4).
     * An operator's <a href="http://en.wikipedia.org/wiki/Operator_associativity">associativity</a> define how operators of the same precedence are grouped.
     */
    public Operator(@NonNull Set<String> names, Associativity unaryAssociativity, Integer unaryPrecedence, Associativity binaryAssociativity, Integer binaryPrecedence) {
        if (unaryAssociativity == null && binaryAssociativity == null) {
            throw new IllegalArgumentException("The operant %s need at least on associativity".formatted(names));
        }
        if (names.size() == 0) {
            throw new IllegalArgumentException("Operator names can't be empty");
        }
        if (names.stream().anyMatch(Strings::isNullOrEmpty)) {
            throw new IllegalArgumentException("Operator name can't be null or empty");
        }
        this.names = ImmutableSet.copyOf(names);
        this.unaryAssociativity = unaryAssociativity;
        this.binaryAssociativity = binaryAssociativity;
        this.unaryPrecedence = unaryPrecedence;
        this.binaryPrecedence = binaryPrecedence;
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

    protected static String getBinaryOperatorExpression(String name, List<Roll> operands) {
        return String.format("%s%s%s", operands.get(0).getExpression(), name, operands.get(1).getExpression());
    }

    protected static String getLeftUnaryExpression(String name, List<Roll> operands) {
        return String.format("%s%s", operands.get(0).getExpression(), name);
    }

    protected static String getRightUnaryExpression(String name, List<Roll> operands) {
        return String.format("%s%s", name, operands.get(0).getExpression());
    }

    public abstract @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) throws ExpressionException;

    public boolean supportUnaryOperation() {
        return unaryAssociativity != null;
    }

    public boolean supportBinaryOperation() {
        return binaryAssociativity != null;
    }

    public @NonNull Set<String> getNames() {
        return names;
    }

    public @NonNull String getName() {
        if (names.size() == 1) {
            return names.iterator().next();
        }
        return names.toString();
    }

    public @NonNull String getPrimaryName() {
        return names.iterator().next();
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
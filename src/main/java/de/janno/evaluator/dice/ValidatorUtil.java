package de.janno.evaluator.dice;

import de.janno.evaluator.ExpressionException;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class ValidatorUtil {
    public static ExpressionException throwNotIntegerExpression(@NonNull String operatorName, @NonNull Result result, @NonNull String side) {
        return new ExpressionException(String.format("Operator '%s' requires as %s operand a single integer but was '%s'", operatorName, side, result.getElements().stream()
                .map(ResultElement::getValue)
                .toList()));
    }

    public static void checkContainsOnlyInteger(@NonNull String operatorName, @NonNull Result result, @NonNull String side) throws ExpressionException {
        if (!result.containsOnlyIntegers()) {
            throw new ExpressionException(String.format("Operator '%s' requires as %s operand only integers but was '%s'", operatorName, side, result.getElements().stream()
                    .map(ResultElement::getValue).toList()));
        }
    }

    public static void checkAllElementsAreSameColor(@NonNull String operatorName, @NonNull Result... results) throws ExpressionException {
        Set<String> allElementColors = Arrays.stream(results).flatMap(r -> r.getElements().stream()).map(ResultElement::getColor).collect(Collectors.toSet());
        if (allElementColors.size() != 1) {
            throw new ExpressionException(String.format("Operator '%s' requires all elements to be the same color, the colors where '%s'", operatorName, allElementColors));
        }
    }
}

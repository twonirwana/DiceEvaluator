package de.janno.evaluator.dice;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class ValidatorUtil {
    public static ExpressionException throwNotIntegerExpression(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) {
        return new ExpressionException(String.format("'%s' requires as %s input a single integer but was '%s'", inputName, location, roll.getElements().stream()
                .map(RollElement::getValue)
                .toList()));
    }

    public static void checkContainsOnlyInteger(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) throws ExpressionException {
        if (!roll.containsOnlyIntegers()) {
            throw new ExpressionException(String.format("'%s' requires as %s input only integers but was '%s'", inputName, location, roll.getElements().stream()
                    .map(RollElement::getValue).toList()));
        }
    }

    public static void checkContainsSingleElement(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) throws ExpressionException {
        if (roll.getElements().size() != 1) {
            throw new ExpressionException(String.format("'%s' requires as %s a single element but was '%s'", inputName, location, roll.getElements().stream()
                    .map(RollElement::getValue).toList()));
        }
    }

    public static void checkAllElementsAreSameColor(@NonNull String operatorName, @NonNull Roll... rolls) throws ExpressionException {
        Set<String> allElementColors = Arrays.stream(rolls).flatMap(r -> r.getElements().stream()).map(RollElement::getColor).collect(Collectors.toSet());
        if (allElementColors.size() != 1) {
            throw new ExpressionException(String.format("'%s' requires all elements to be the same color, the colors where '%s'", operatorName, allElementColors));
        }
    }
}

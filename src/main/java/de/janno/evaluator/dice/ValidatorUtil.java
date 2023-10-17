package de.janno.evaluator.dice;

import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ValidatorUtil {
    public static ExpressionException throwNotIntegerExpression(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) {
        return new ExpressionException(String.format("'%s' requires as %s input a single integer but was '%s'%s", inputName, location, roll.getElements().stream()
                .map(RollElement::getValue)
                .toList(), getSumHelp(roll)));
    }

    public static ExpressionException throwNotBoolean(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) {
        return new ExpressionException(String.format("'%s' requires as %s input a single boolean but was '%s'", inputName, location, roll.getElements().stream()
                .map(RollElement::getValue)
                .toList()));
    }


    public static ExpressionException throwNotDecimalExpression(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) {
        return new ExpressionException(String.format("'%s' requires as %s input a single decimal but was '%s'%s", inputName, location, roll.getElements().stream()
                .map(RollElement::getValue)
                .toList(), getSumHelp(roll)));
    }


    private static String getSumHelp(@NonNull Roll roll) {
        boolean numberList = roll.getElements().stream().map(RollElement::asInteger).allMatch(Optional::isPresent);
        return numberList ? ". Try to sum the numbers together like (%s=)".formatted(roll.getExpression()) : "";
    }

    public static void checkContainsOnlyDecimal(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) throws ExpressionException {
        if (!roll.containsOnlyDecimals()) {
            throw new ExpressionException(String.format("'%s' requires as %s input only decimals but was '%s'", inputName, location, roll.getElements().stream()
                    .map(RollElement::getValue).toList()));
        }
    }

    public static void checkContainsSingleElement(@NonNull String inputName, @NonNull Roll roll, @NonNull String location) throws ExpressionException {
        if (roll.getElements().size() != 1) {
            throw new ExpressionException(String.format("'%s' requires as %s a single element but was '%s'%s", inputName, location, roll.getElements().stream()
                            .map(RollElement::getValue).toList(),
                    getSumHelp(roll)
            ));
        }
    }

    public static void checkRollSize(@NonNull String inputName, @NonNull List<Roll> rolls, int minInc, int maxInc) throws ExpressionException {
        if (rolls.size() < minInc || rolls.size() > maxInc) {
            String range = minInc == maxInc ? String.valueOf(minInc) : "%d-%d".formatted(minInc, maxInc);
            throw new ExpressionException(String.format("'%s' requires as %s inputs but was '%s'", inputName, range, rolls.stream()
                    .map(Roll::getElements).toList()
            ));
        }
    }

    public static void checkAllElementsAreSameTag(@NonNull String operatorName, @NonNull Roll... rolls) throws ExpressionException {
        Set<String> allElementTags = Arrays.stream(rolls).flatMap(r -> r.getElements().stream()).map(RollElement::getTag).collect(Collectors.toSet());
        if (allElementTags.size() != 1) {
            throw new ExpressionException(String.format("'%s' requires all elements to be the same tag, the tags where '%s'", operatorName, allElementTags));
        }
    }
}

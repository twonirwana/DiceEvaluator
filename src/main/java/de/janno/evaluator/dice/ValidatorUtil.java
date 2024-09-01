package de.janno.evaluator.dice;

import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ValidatorUtil {
    public static ExpressionException throwNotIntegerExpression(@NonNull ExpressionPosition expressionPosition, @NonNull Roll roll, @NonNull String location) {
        return new ExpressionException(String.format("'%s' requires as %s input a single integer but was '%s'%s", expressionPosition.getValue(), location, roll.getElements().stream()
                .map(RollElement::getValue)
                .toList(), getSumHelp(roll)), expressionPosition);
    }

    public static ExpressionException throwNotBoolean(@NonNull ExpressionPosition expressionPosition, @NonNull Roll roll, @NonNull String location) {
        return new ExpressionException(String.format("'%s' requires as %s input a single boolean but was '%s'", expressionPosition.getValue(), location, roll.getElements().stream()
                .map(RollElement::getValue)
                .toList()), expressionPosition);
    }


    public static ExpressionException throwNotDecimalExpression(@NonNull ExpressionPosition expressionPosition, @NonNull Roll roll, @NonNull String location) {
        return new ExpressionException(String.format("'%s' requires as %s input a single decimal but was '%s'%s", expressionPosition.getValue(), location, roll.getElements().stream()
                .map(RollElement::getValue)
                .toList(), getSumHelp(roll)), expressionPosition);
    }


    private static String getSumHelp(@NonNull Roll roll) {
        boolean numberList = roll.getElements().stream().map(RollElement::asInteger).allMatch(Optional::isPresent);
        return numberList ? ". Try to sum the numbers together like (%s=)".formatted(roll.getExpression()) : "";
    }

    public static void checkContainsOnlyDecimal(@NonNull ExpressionPosition expressionPosition, @NonNull Roll roll, @NonNull String location) throws ExpressionException {
        if (!roll.containsOnlyDecimals()) {
            throw new ExpressionException(String.format("'%s' requires as %s input only decimals but was '%s'", expressionPosition.getValue(), location, roll.getElements().stream()
                    .map(RollElement::getValue).toList()), expressionPosition);
        }
    }

    public static void checkContainsSingleElement(@NonNull ExpressionPosition expressionPosition, @NonNull Roll roll, @NonNull String location) throws ExpressionException {
        if (roll.getElements().size() != 1) {
            throw new ExpressionException(String.format("'%s' requires as %s a single element but was '%s'%s", expressionPosition.getValue(), location, roll.getElements().stream()
                            .map(RollElement::getValue).toList(),
                    getSumHelp(roll)
            ), expressionPosition);
        }
    }

    public static void checkContainsNoOrSingleElement(@NonNull ExpressionPosition expressionPosition, @NonNull Roll roll, @NonNull String location) throws ExpressionException {
        if (!(roll.getElements().size() == 1 || roll.getElements().isEmpty())) {
            throw new ExpressionException(String.format("'%s' requires as %s a single or no element but was '%s'%s", expressionPosition.getValue(), location, roll.getElements().stream()
                            .map(RollElement::getValue).toList(),
                    getSumHelp(roll)
            ), expressionPosition);
        }
    }

    public static void checkRollSize(@NonNull ExpressionPosition expressionPosition, @NonNull List<Roll> rolls, int minInc, int maxInc) throws ExpressionException {
        if (rolls.size() < minInc || rolls.size() > maxInc) {
            String range = minInc == maxInc ? String.valueOf(minInc) : "%d-%d".formatted(minInc, maxInc);
            throw new ExpressionException(String.format("'%s' requires as %s inputs but was '%s'", expressionPosition.getValue(), range, rolls.stream()
                    .map(Roll::getElements).toList()
            ), expressionPosition);
        }
    }

    public static void checkRollSize(@NonNull ExpressionPosition expressionPosition, @NonNull Optional<List<Roll>> rolls, int minInc, int maxInc) throws ExpressionException {
        String range = minInc == maxInc ? String.valueOf(minInc) : "%d-%d".formatted(minInc, maxInc);
        if (rolls.isEmpty()) {
            throw new ExpressionException(String.format("'%s' requires as %s inputs but was empty", expressionPosition.getValue(), range), expressionPosition);
        }
        if (rolls.get().size() < minInc || rolls.get().size() > maxInc) {

            throw new ExpressionException(String.format("'%s' requires as %s inputs but was '%s'", expressionPosition.getValue(), range, rolls.get().stream()
                    .map(Roll::getElements).toList()
            ), expressionPosition);
        }
    }

    public static void checkAllElementsAreSameTag(@NonNull ExpressionPosition expressionPosition, @NonNull Roll... rolls) throws ExpressionException {
        Set<String> allElementTags = Arrays.stream(rolls).flatMap(r -> r.getElements().stream()).map(RollElement::getTag).collect(Collectors.toSet());
        if (allElementTags.size() != 1) {
            throw new ExpressionException(String.format("'%s' requires all elements to be the same tag, the tags where '%s'", expressionPosition.getValue(), allElementTags), expressionPosition);
        }
    }
}

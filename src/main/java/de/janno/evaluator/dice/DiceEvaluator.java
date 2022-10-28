package de.janno.evaluator.dice;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import de.janno.evaluator.*;
import de.janno.evaluator.dice.function.*;
import de.janno.evaluator.dice.function.Double;
import de.janno.evaluator.dice.operator.die.ExplodingAddDice;
import de.janno.evaluator.dice.operator.die.ExplodingDice;
import de.janno.evaluator.dice.operator.die.RegularDice;
import de.janno.evaluator.dice.operator.list.*;
import de.janno.evaluator.dice.operator.math.Divide;
import de.janno.evaluator.dice.operator.math.Multiply;
import de.janno.evaluator.dice.operator.math.NegateOrNegativAppending;
import de.janno.evaluator.dice.operator.math.Appending;
import lombok.NonNull;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceEvaluator extends AbstractEvaluator<Roll> {

    private static final int DEFAULT_MAX_NUMBER_OF_DICE = 1000;

    private static final Pattern LIST_REGEX = Pattern.compile("(.+(/.+)+)");

    public DiceEvaluator() {
        this(new RandomNumberSupplier(), DEFAULT_MAX_NUMBER_OF_DICE);
    }

    public DiceEvaluator(@NonNull NumberSupplier numberSupplier, int maxNumberOfDice) {
        super(Parameters.<Roll>builder()
                .expressionBracket(BracketPair.PARENTHESES)
                .functionBracket(BracketPair.PARENTHESES)
                .escapeBracket(BracketPair.APOSTROPHE)
                .escapeBracket(BracketPair.BRACKETS)
                .operators(ImmutableList.<Operator<Roll>>builder()
                        .add(new RegularDice(numberSupplier, maxNumberOfDice))
                        .add(new ExplodingDice(numberSupplier, maxNumberOfDice))
                        .add(new ExplodingAddDice(numberSupplier, maxNumberOfDice))
                        .add(new Appending())
                        .add(new Sum())
                        .add(new NegateOrNegativAppending())
                        .add(new Divide())
                        .add(new Multiply())
                        .add(new KeepHighest())
                        .add(new KeepLowest())
                        .add(new GreaterThanFilter())
                        .add(new LesserThanFilter())
                        .add(new GreaterEqualThanFilter())
                        .add(new LesserEqualThanFilter())
                        .add(new Count())
                        .build())
                .functions(ImmutableList.<Function<Roll>>builder()
                        .add(new Color())
                        .add(new SortAsc())
                        .add(new SortDesc())
                        .add(new Min())
                        .add(new Max())
                        .add(new Cancel())
                        .add(new Double())
                        .add(new IfEqual())
                        .add(new IfGreater())
                        .add(new IfLesser())
                        .build())
                .separator(",")
                .build());
    }

    @Override
    protected @NonNull Roll toValue(@NonNull String literal) {
        Matcher matcher = LIST_REGEX.matcher(literal);
        if (matcher.find()) {
            //Todo is this needed: ('Head'+'Tail') is the same as [Head,Tail] but needs escaping
            List<String> list = Arrays.asList(matcher.group(1).split("/"));
            return new Roll(list.toString(), list.stream()
                    .map(String::trim)
                    .map(s -> new RollElement(s, RollElement.NO_COLOR))
                    .collect(ImmutableList.toImmutableList()), ImmutableList.of(), ImmutableList.of());
        }
        return new Roll(literal, ImmutableList.of(new RollElement(literal, RollElement.NO_COLOR)), ImmutableList.of(), ImmutableList.of());
    }

    @Override
    public @NonNull List<Roll> evaluate(@NonNull String expression) throws ExpressionException {
        expression = expression.trim();
        if (Strings.isNullOrEmpty(expression)) {
            return ImmutableList.of(new Roll("", ImmutableList.of(), ImmutableList.of(), ImmutableList.of()));
        }
        return super.evaluate(expression);
    }

    public static String getHelpText() {
        URL url = Resources.getResource("help.md");
        try {
            return Resources.toString(url, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

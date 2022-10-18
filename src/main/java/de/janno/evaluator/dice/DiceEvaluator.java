package de.janno.evaluator.dice;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import de.janno.evaluator.*;
import de.janno.evaluator.dice.function.*;
import de.janno.evaluator.dice.operator.die.ExplodingAddDice;
import de.janno.evaluator.dice.operator.die.ExplodingDice;
import de.janno.evaluator.dice.operator.die.RegularDice;
import de.janno.evaluator.dice.operator.list.*;
import de.janno.evaluator.dice.operator.math.Divide;
import de.janno.evaluator.dice.operator.math.Multiply;
import de.janno.evaluator.dice.operator.math.NegateOrNegativUnion;
import de.janno.evaluator.dice.operator.math.Union;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceEvaluator extends AbstractEvaluator<Result> {

    private static final int DEFAULT_MAX_NUMBER_OF_DICE = 1000;

    private static final Pattern LIST_REGEX = Pattern.compile("(.+(/.+)+)");

    public DiceEvaluator() {
        this(new RandomNumberSupplier(), DEFAULT_MAX_NUMBER_OF_DICE);
    }

    public DiceEvaluator(@NonNull NumberSupplier numberSupplier, int maxNumberOfDice) {
        super(Parameters.<Result>builder()
                .expressionBracket(BracketPair.PARENTHESES)
                .functionBracket(BracketPair.PARENTHESES)
                .escapeBracket(BracketPair.APOSTROPHE)
                .escapeBracket(BracketPair.BRACKETS)
                .operators(ImmutableList.<Operator<Result>>builder()
                        .add(new RegularDice(numberSupplier, maxNumberOfDice))
                        .add(new ExplodingDice(numberSupplier, maxNumberOfDice))
                        .add(new ExplodingAddDice(numberSupplier, maxNumberOfDice))
                        .add(new Union())
                        .add(new Sum())
                        .add(new NegateOrNegativUnion())
                        .add(new Divide())
                        .add(new Multiply())
                        .add(new KeepHighest())
                        .add(new KeepLowest())
                        .add(new GreaterThanFilter())
                        .add(new LesserThanFilter())
                        .add(new Count())
                        .build())
                .functions(ImmutableList.<Function<Result>>builder()
                        .add(new Color())
                        .add(new SortAsc())
                        .add(new SortDesc())
                        .add(new Min())
                        .add(new Max())
                        .build())
                .separator(",")
                .build());
    }

    @Override
    protected @NonNull Result toValue(@NonNull String literal) {
        Matcher matcher = LIST_REGEX.matcher(literal);
        if (matcher.find()) {
            List<String> list = Arrays.asList(matcher.group(1).split("/"));
            return new Result(list.toString(), list.stream()
                    .map(String::trim)
                    .map(s -> new ResultElement(s, ResultElement.NO_COLOR))
                    .collect(ImmutableList.toImmutableList()), ImmutableList.of(), ImmutableList.of());
        }
        return new Result(literal, ImmutableList.of(new ResultElement(literal, ResultElement.NO_COLOR)), ImmutableList.of(), ImmutableList.of());
    }

    @Override
    public @NonNull List<Result> evaluate(@NonNull String expression) throws ExpressionException {
        expression = expression.trim();
        if (Strings.isNullOrEmpty(expression)) {
            return ImmutableList.of(new Result("", ImmutableList.of(), ImmutableList.of(), ImmutableList.of()));
        }
        return super.evaluate(expression);
    }
}

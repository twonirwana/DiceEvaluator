package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static de.janno.evaluator.dice.ValidatorUtil.checkContainsSingleElement;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Regex extends Function {
    public Regex(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("regex", 3, Integer.MAX_VALUE, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                if (arguments.size() % 2 == 0) {
                    throw new ExpressionException(String.format("'%s' requires an odd number of arguments but was %d", getName(), arguments.size()), expressionPosition);
                }
                Optional<List<Roll>> input = arguments.getFirst().extendRoll(rollContext);
                if (input.isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as first argument", expressionPosition.getValue()), expressionPosition);
                }
                ImmutableList.Builder<Roll> allRolls = ImmutableList.builder();
                ImmutableList.Builder<Roll> rollExpression = ImmutableList.builder();
                allRolls.addAll(input.get());
                rollExpression.addAll(input.get());
                ImmutableList<RollElement> inputRollElements = input.get().stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList());
                ImmutableList.Builder<Roll> childrenRollBuilder = ImmutableList.<Roll>builder()
                        .addAll(input.get());
                for (int i = 1; i < arguments.size() - 1; i = i + 2) {
                    Optional<List<Roll>> regexRolls = arguments.get(i).extendRoll(rollContext);
                    checkRollSize(expressionPosition, regexRolls, 1, 1);
                    Roll regexRoll = regexRolls.orElseThrow().getFirst();
                    childrenRollBuilder.add(regexRoll);
                    allRolls.add(regexRoll);
                    checkContainsSingleElement(expressionPosition, regexRoll, "%d argument".formatted(i));
                    final Pattern pattern;
                    //todo test
                    try {
                        pattern = Pattern.compile(regexRoll.asSingleValue().orElseThrow());
                    } catch (PatternSyntaxException e) {
                        throw new ExpressionException(String.format("'%s' is invalid regex", regexRoll.asSingleValue().orElseThrow()), expressionPosition);
                    }
                    ImmutableList.Builder<RollElement> currentIterationElements = ImmutableList.builder();
                    for (RollElement rollElement : inputRollElements) {
                        Matcher matcher = pattern.matcher(rollElement.getValue());
                        if (matcher.find()) {
                            Optional<List<Roll>> replaceRolls = arguments.get(i + 1).extendRoll(rollContext);
                            checkRollSize(expressionPosition, replaceRolls, 1, 1);
                            Roll replaceRoll = replaceRolls.orElseThrow().getFirst();
                            childrenRollBuilder.add(replaceRoll);
                            allRolls.add(regexRoll);
                            checkContainsSingleElement(expressionPosition, replaceRoll, "%d argument".formatted(i + 1));
                            String regexReplaceString = replaceRoll.asSingleValue().orElseThrow();
                            String replacement = matcher.replaceAll(regexReplaceString);
                            currentIterationElements.add(new RollElement(replacement, rollElement.getTag(), rollElement.getColor()));
                        } else {
                            currentIterationElements.add(rollElement);
                        }
                    }
                    inputRollElements = currentIterationElements.build();
                }

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        inputRollElements,
                        RandomElementsBuilder.fromRolls(allRolls.build(), rollContext),
                        childrenRollBuilder.build(),
                        expressionPosition,
                        maxNumberOfElements, keepChildrenRolls)));

            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition, arguments);
            }
        };
    }
}
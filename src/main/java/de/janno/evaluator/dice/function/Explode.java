package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;

public class Explode extends Function {
    public Explode(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("exp", 2, 3, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                if (arguments.size() != 2 && arguments.size() != 3) {
                    throw new ExpressionException(String.format("'%s' requires 2 or 3 arguments but was %d", getName(), arguments.size()));
                }
                ImmutableList.Builder<Roll> allChildrenRollBuilder = ImmutableList.builder();
                ImmutableList.Builder<Roll> rollExpression = ImmutableList.builder();
                RollBuilder inputRoll = arguments.getFirst();
                final List<Roll> firstRoll = inputRoll.extendRoll(rollContext).orElse(Collections.emptyList());
                rollExpression.addAll(firstRoll);
                allChildrenRollBuilder.addAll(firstRoll);

                Optional<List<Roll>> compareToRoll = arguments.get(1).extendRoll(rollContext);
                if (compareToRoll.isEmpty()) {
                    throw new ExpressionException(String.format("'%s' requires a non-empty input as second argument", expressionPosition.value()));
                }
                checkRollSize(expressionPosition.value(), compareToRoll.get(), 1, 1);
                Roll compareTo = compareToRoll.get().getFirst();
                allChildrenRollBuilder.add(compareTo);
                rollExpression.add(compareTo);

                final int maxNumberOfRerolls;
                if (arguments.size() == 3) {
                    Optional<List<Roll>> maxRerollsRoll = arguments.get(2).extendRoll(rollContext);
                    if (maxRerollsRoll.isEmpty()) {
                        throw new ExpressionException(String.format("'%s' requires a non-empty input as third argument", expressionPosition.value()));
                    }
                    checkRollSize(expressionPosition.value(), maxRerollsRoll.get(), 1, 1);
                    Roll maxNumberOfRerollsRoll = maxRerollsRoll.get().getFirst();
                    maxNumberOfRerolls = maxNumberOfRerollsRoll.asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition.value(), maxRerollsRoll.get().getFirst(), "third argument"));
                    if (maxNumberOfRerolls > 100 || maxNumberOfRerolls < 0) {
                        throw new ExpressionException(String.format("'%s' requires as third argument a number between 0 and 100", expressionPosition.value()));
                    }
                    allChildrenRollBuilder.add(maxNumberOfRerollsRoll);
                    rollExpression.add(maxNumberOfRerollsRoll);
                } else {
                    maxNumberOfRerolls = 100;
                }

                int rerolls = 0;
                List<Roll> lastRoll = firstRoll;
                ImmutableList.Builder<Roll> allResultRollsBuilder = ImmutableList.builder();
                allResultRollsBuilder.addAll(firstRoll);
                while (rerolls < maxNumberOfRerolls && lastRoll.stream().flatMap(r -> r.getElements().stream()).anyMatch(compareTo::isElementsContainsElementWithValueAndTag)) {
                    lastRoll = inputRoll.extendRoll(rollContext).orElse(Collections.emptyList());
                    allResultRollsBuilder.addAll(lastRoll);
                    allChildrenRollBuilder.addAll(lastRoll);
                    rerolls++;
                }
                final ImmutableList<Roll> allResultRolls = allResultRollsBuilder.build();
                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        allResultRolls.stream().flatMap(r -> r.getElements().stream()).collect(ImmutableList.toImmutableList()),
                        UniqueRandomElements.from(allChildrenRollBuilder.build()), allChildrenRollBuilder.build(),
                        maxNumberOfElements,
                        keepChildrenRolls)));
            }

            @Override
            public @NonNull String toExpression() {
                return getExpression(expressionPosition.value(), arguments);
            }
        };

    }
}
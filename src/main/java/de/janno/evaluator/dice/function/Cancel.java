package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Cancel extends Function {
    public Cancel() {
        super("cancel", 3);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull String inputValue) throws ExpressionException {
        return variables -> {
            List<Roll> rolls = extendAllBuilder(arguments, variables);
            checkRollSize(inputValue, rolls, getMinArgumentCount(), getMaxArgumentCount());
            Roll input = rolls.getFirst();
            Roll typeA = rolls.get(1);
            Roll typeB = rolls.get(2);

            List<RollElement> noMatch = input.getElements().stream()
                    .filter(r -> !typeA.isElementsContainsElementWithValueAndTag(r) && !typeB.isElementsContainsElementWithValueAndTag(r))
                    .collect(ImmutableList.toImmutableList());
            List<RollElement> typeAMatch = input.getElements().stream()
                    .filter(typeA::isElementsContainsElementWithValueAndTag)
                    .collect(ImmutableList.toImmutableList());
            List<RollElement> typeBMatch = input.getElements().stream()
                    .filter(typeB::isElementsContainsElementWithValueAndTag)
                    .collect(ImmutableList.toImmutableList());

            ImmutableList.Builder<RollElement> resultBuilder = ImmutableList.<RollElement>builder()
                    .addAll(noMatch);

            if (typeAMatch.size() > typeBMatch.size()) {
                resultBuilder.addAll(getChancel(typeAMatch, typeBMatch));
            } else if (typeAMatch.size() < typeBMatch.size()) {
                resultBuilder.addAll(getChancel(typeBMatch, typeAMatch));
            }
            return Optional.of(ImmutableList.of(new Roll(getExpression(inputValue, rolls),
                    resultBuilder.build(),
                    UniqueRandomElements.from(rolls),
                    ImmutableList.<Roll>builder()
                            .addAll(input.getChildrenRolls())
                            .addAll(typeA.getChildrenRolls())
                            .addAll(typeB.getChildrenRolls())
                            .build())));
        };
    }

    private List<RollElement> getChancel(List<RollElement> bigger, List<RollElement> smaller) {
        return bigger.subList(smaller.size(), bigger.size());
    }
}
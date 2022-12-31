package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Cancel extends Function {
    public Cancel() {
        super("cancel", 3);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            checkRollSize(getName(), rolls, getMinArgumentCount(), getMaxArgumentCount());
            Roll input = rolls.get(0);
            Roll typeA = rolls.get(1);
            Roll typeB = rolls.get(2);

            List<RollElement> noMatch = input.getElements().stream()
                    .filter(r -> !typeA.getElements().contains(r) && !typeB.getElements().contains(r))
                    .collect(ImmutableList.toImmutableList());
            List<RollElement> typeAMatch = input.getElements().stream()
                    .filter(r -> typeA.getElements().contains(r))
                    .collect(ImmutableList.toImmutableList());
            List<RollElement> typeBMatch = input.getElements().stream()
                    .filter(r -> typeB.getElements().contains(r))
                    .collect(ImmutableList.toImmutableList());

            ImmutableList.Builder<RollElement> resultBuilder = ImmutableList.<RollElement>builder()
                    .addAll(noMatch);

            if (typeAMatch.size() > typeBMatch.size()) {
                resultBuilder.addAll(getChancel(typeAMatch, typeBMatch));
            } else if (typeAMatch.size() < typeBMatch.size()) {
                resultBuilder.addAll(getChancel(typeBMatch, typeAMatch));
            }
            return ImmutableList.of(new Roll(getExpression(getName(), rolls),
                    resultBuilder.build(),
                    UniqueRandomElements.from(rolls),
                    ImmutableList.<Roll>builder()
                            .addAll(input.getChildrenRolls())
                            .addAll(typeA.getChildrenRolls())
                            .addAll(typeB.getChildrenRolls())
                            .build()));
        };
    }

    private List<RollElement> getChancel(List<RollElement> bigger, List<RollElement> smaller) {
        return bigger.subList(smaller.size(), bigger.size());
    }
}
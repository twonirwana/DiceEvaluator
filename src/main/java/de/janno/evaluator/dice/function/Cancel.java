package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;

public class Cancel extends Function {
    public Cancel() {
        super("cancel", 3);
    }

    @Override
    public @NonNull Roll evaluate(@NonNull List<Roll> arguments) throws ExpressionException {
        Roll input = arguments.get(0);
        Roll typeA = arguments.get(1);
        Roll typeB = arguments.get(2);

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
        return new Roll(getExpression(getPrimaryName(), arguments),
                resultBuilder.build(),
                UniqueRandomElements.from(arguments),
                ImmutableList.<Roll>builder()
                        .addAll(input.getChildrenRolls())
                        .addAll(typeA.getChildrenRolls())
                        .addAll(typeB.getChildrenRolls())
                        .build(), null);
    }

    private List<RollElement> getChancel(List<RollElement> bigger, List<RollElement> smaller) {
        return bigger.subList(smaller.size(), bigger.size());
    }
}
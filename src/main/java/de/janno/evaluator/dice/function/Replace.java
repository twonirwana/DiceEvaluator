package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Stream;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Replace extends Function {
    public Replace() {
        super("replace", 3);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            checkRollSize(getName(), rolls, getMinArgumentCount(), getMaxArgumentCount());

            Roll input = rolls.get(0);
            Roll find = rolls.get(1);
            Roll replace = rolls.get(2);

            ImmutableList<RollElement> rollElements = input.getElements().stream()
                    .flatMap(r -> {
                        if (find.getElements().contains(r)) {
                            return replace.getElements().stream();
                        }
                        return Stream.of(r);
                    })
                    .collect(ImmutableList.toImmutableList());


            return ImmutableList.of(new Roll(getExpression(getPrimaryName(), rolls),
                    rollElements,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.<Roll>builder()
                            .addAll(input.getChildrenRolls())
                            .addAll(find.getChildrenRolls())
                            .addAll(replace.getChildrenRolls())
                            .build()));
        };
    }
}
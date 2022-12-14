package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.checkRollSize;

public class Concat extends Function {
    public Concat() {
        super("concat", 2, Integer.MAX_VALUE);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = extendAllBuilder(arguments, constants);
            checkRollSize(getName(), rolls, getMinArgumentCount(), getMaxArgumentCount());
            String joined = rolls.stream()
                    .map(Roll::getResultString)
                    .collect(Collectors.joining());
            return ImmutableList.of(new Roll(getExpression(getName(), rolls),
                    ImmutableList.of(new RollElement(joined, RollElement.NO_COLOR)),
                    UniqueRandomElements.from(rolls),
                    ImmutableList.copyOf(rolls)));
        };
    }
}

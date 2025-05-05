package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.RollBuilder.extendAllBuilder;
import static de.janno.evaluator.dice.ValidatorUtil.*;

public class Round extends Function {
    private final static Set<String> ROUND_MODES = Arrays.stream(RoundingMode.values())
            .map(RoundingMode::toString)
            .collect(Collectors.toSet());

    public Round(int maxNumberOfElements, boolean keepChildrenRolls) {
        super("round", 2, 3, maxNumberOfElements, keepChildrenRolls);
    }

    @Override
    public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> arguments, @NonNull ExpressionPosition expressionPosition) throws ExpressionException {
        return new RollBuilder() {
            @Override
            public @NonNull Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException {
                List<Roll> rolls = extendAllBuilder(arguments, rollContext);
                checkRollSize(expressionPosition, rolls, getMinArgumentCount(), getMaxArgumentCount());

                BigDecimal number = rolls.getFirst().asDecimal().orElseThrow(() -> throwNotDecimalExpression(expressionPosition, rolls.getFirst(), "first argument"));

                Roll modeRoll = rolls.get(1);
                Optional<String> roundMethodeRoll = modeRoll.asSingleValue();
                if (roundMethodeRoll.isEmpty() || !ROUND_MODES.contains(roundMethodeRoll.get().toUpperCase())) {
                    throw new ExpressionException("The second element must be a single value of: UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN but was: " + modeRoll.getResultString(), expressionPosition);
                }

                RoundingMode roundingMode = RoundingMode.valueOf(roundMethodeRoll.get().toUpperCase());

                final int scale;
                if (rolls.size() > 2) {
                    scale = rolls.get(2).asInteger().orElseThrow(() -> throwNotIntegerExpression(expressionPosition, rolls.get(2), "third argument"));
                } else {
                    scale = 0;
                }

                BigDecimal result = number.setScale(scale, roundingMode);

                RollElement numberRollElement = rolls.getFirst().getElements().getFirst();

                return Optional.of(ImmutableList.of(new Roll(toExpression(),
                        ImmutableList.of(new RollElement(result.toString(), numberRollElement.getTag(), numberRollElement.getColor())),
                        RandomElementsBuilder.fromRolls(rolls, rollContext),
                        ImmutableList.copyOf(rolls),
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

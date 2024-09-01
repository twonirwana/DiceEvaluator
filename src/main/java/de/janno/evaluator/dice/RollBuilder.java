package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface RollBuilder {
    static ImmutableList<Roll> extendAllBuilder(@NonNull List<RollBuilder> rollBuilders, @NonNull RollContext rollContext) throws ExpressionException {
        ImmutableList.Builder<Roll> builder = ImmutableList.builder();
        for (RollBuilder rs : rollBuilders) {
            extendRollBuilder(rollContext, rs, builder);
        }
        return builder.build();
    }

    static void extendRollBuilder(@NonNull RollContext rollContext, @NonNull RollBuilder rs, @NonNull ImmutableList.Builder<Roll> builder) throws ExpressionException {
        Optional<List<Roll>> r = rs.extendRoll(rollContext);
        //don't add empty rolls, they are the result from the value function and change the number of rolls (and then the operator/function
        //uses the roll with the wrong index
        r.ifPresent(builder::addAll);
    }

    /**
     * Creates a concrete roll from a roll builder (applies all random function aka throwing the dice).
     * <p>
     * Some functions or operators (e.g. val) produces empty results, they are filtered and therefore don't influence the argument count in functions.
     * This makes it possible use a val function inside an if function because if the val would return an empty list then it would be an argument for if.
     * This is not a problem in operators because there the number of arguments is always correct because val is already pushed on the result stack
     */
    @NonNull
    Optional<List<Roll>> extendRoll(@NonNull RollContext rollContext) throws ExpressionException;

    @NonNull
    String toExpression();
}

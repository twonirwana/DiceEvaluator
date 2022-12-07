package de.janno.evaluator.dice.function;

import com.google.common.collect.ImmutableMap;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public class Value extends Function {
    public Value() {
        super("val", 2);
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> arguments) throws ExpressionException {
        return new RollSupplier() {

            @Override
            public Roll roll() throws ExpressionException {
                throw new IllegalStateException("provides only a constant");
            }

            @Override
            public boolean isConstant() {
                return true;
            }

            @Override
            public Map<String, Roll> getConstant() throws ExpressionException {
                List<Roll> rolls = EvaluationUtils.rollAllSupplier(arguments);
                return ImmutableMap.of(rolls.get(0).getElements().get(0).getValue(), new Roll(getExpression(Value.this.getPrimaryName(), rolls),
                        rolls.get(1).getElements(),
                        UniqueRandomElements.from(rolls),
                        rolls.get(1).getChildrenRolls()));
            }
        };
    }
}

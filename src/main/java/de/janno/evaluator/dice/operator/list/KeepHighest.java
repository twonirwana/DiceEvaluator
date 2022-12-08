package de.janno.evaluator.dice.operator.list;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.*;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.janno.evaluator.dice.EvaluationUtils.rollAllSupplier;
import static de.janno.evaluator.dice.ValidatorUtil.throwNotIntegerExpression;
import static de.janno.evaluator.dice.operator.OperatorOrder.getOderNumberOf;

public class KeepHighest extends Operator {

    public KeepHighest() {
        super(Set.of("k", "K"), null, null, Operator.Associativity.LEFT, getOderNumberOf(KeepHighest.class));
    }

    @Override
    public @NonNull RollSupplier evaluate(@NonNull List<RollSupplier> operands) throws ExpressionException {
        return constants -> {
            List<Roll> rolls = rollAllSupplier(operands, constants);
            Roll left = rolls.get(0);
            Roll right = rolls.get(1);
            final int rightNumber = right.asInteger().orElseThrow(() -> throwNotIntegerExpression(getName(), right, "right"));
            //todo right color only filtered by same color?
            ImmutableList<RollElement> keep = left.getElements().stream()
                    .collect(Collectors.groupingBy(RollElement::getColor)).values().stream()
                    .flatMap(cl -> cl.stream()
                            .sorted(Comparator.reverseOrder())
                            .limit(rightNumber)
                    )
                    .collect(ImmutableList.toImmutableList());
            return new Roll(getBinaryOperatorExpression(getPrimaryName(), rolls),
                    keep,
                    UniqueRandomElements.from(rolls),
                    ImmutableList.of(left, right));
        };
    }
}

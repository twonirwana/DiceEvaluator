package de.janno.evaluator.dice.operator;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.Operator;
import de.janno.evaluator.dice.Result;
import de.janno.evaluator.dice.operator.die.ExplodingAddDice;
import de.janno.evaluator.dice.operator.die.ExplodingDice;
import de.janno.evaluator.dice.operator.die.RegularDice;
import de.janno.evaluator.dice.operator.list.*;
import de.janno.evaluator.dice.operator.math.Divide;
import de.janno.evaluator.dice.operator.math.NegateOrNegativUnion;
import de.janno.evaluator.dice.operator.math.Multiply;
import de.janno.evaluator.dice.operator.math.Union;


public final class OperatorOrder {

    final static ImmutableList<Class<? extends Operator<Result>>> operatorOrderList =
            ImmutableList.<Class<? extends Operator<Result>>>builder()
                    .add(Sum.class) //sum must be applied as last
                    .add(Union.class)
                    .add(NegateOrNegativUnion.class)
                    .add(Multiply.class)
                    .add(Divide.class)
                    .add(Count.class) //count should be after all list filters
                    .add(GreaterThanFilter.class)
                    .add(LesserThanFilter.class)
                    .add(GreaterEqualThanFilter.class)
                    .add(LesserEqualThanFilter.class)
                    .add(KeepHighest.class)
                    .add(KeepLowest.class)
                    //dice should be first
                    .add(ExplodingAddDice.class)
                    .add(ExplodingDice.class)
                    .add(RegularDice.class)
                    .build();

    public static int getOderNumberOf(Class<? extends Operator<Result>> operatorDefinitionClass) {
        return operatorOrderList.indexOf(operatorDefinitionClass);
    }
}

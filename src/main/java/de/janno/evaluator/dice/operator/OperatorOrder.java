package de.janno.evaluator.dice.operator;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.Operator;
import de.janno.evaluator.dice.operator.die.ExplodingAddDice;
import de.janno.evaluator.dice.operator.die.ExplodingDice;
import de.janno.evaluator.dice.operator.die.RegularDice;
import de.janno.evaluator.dice.operator.die.Reroll;
import de.janno.evaluator.dice.operator.list.*;
import de.janno.evaluator.dice.operator.math.*;


public final class OperatorOrder {

    final static ImmutableList<Class<? extends Operator>> operatorOrderList =
            ImmutableList.<Class<? extends Operator>>builder()
                    .add(Repeat.class)
                    .add(RepeatList.class)
                    .add(Sum.class) //sum must be applied as last
                    .add(Modulo.class)
                    .add(Multiply.class)
                    .add(Divide.class)
                    .add(Count.class) //count should be after all list filters
                    .add(GreaterThanFilter.class)
                    .add(LesserThanFilter.class)
                    .add(GreaterEqualThanFilter.class)
                    .add(LesserEqualThanFilter.class)
                    .add(EqualFilter.class)
                    .add(KeepHighest.class)
                    .add(KeepLowest.class)
                    //dice should be first
                    .add(Appending.class)
                    .add(NegateOrNegativAppending.class)
                    .add(Reroll.class)
                    .add(ExplodingAddDice.class)
                    .add(ExplodingDice.class)
                    .add(RegularDice.class)
                    .build();

    public static int getOderNumberOf(Class<? extends Operator> operatorDefinitionClass) {
        return operatorOrderList.indexOf(operatorDefinitionClass);
    }
}

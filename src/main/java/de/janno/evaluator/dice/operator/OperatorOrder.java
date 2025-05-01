package de.janno.evaluator.dice.operator;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.Operator;
import de.janno.evaluator.dice.operator.bool.*;
import de.janno.evaluator.dice.operator.die.*;
import de.janno.evaluator.dice.operator.list.*;
import de.janno.evaluator.dice.operator.math.*;


public final class OperatorOrder {

    final static ImmutableList<Class<? extends Operator>> operatorOrderList =
            ImmutableList.<Class<? extends Operator>>builder()
                    .add(Repeat.class)
                    .add(RepeatList.class)
                    .add(Concat.class)
                    .add(OrBool.class)
                    .add(AndBool.class)
                    .add(NegateBool.class)
                    .add(EqualBool.class)
                    .add(LesserBool.class)
                    .add(LesserEqualBool.class)
                    .add(GreaterBool.class)
                    .add(GreaterEqualBool.class)
                    .add(InBool.class) //boolean must be applied as last operator
                    .add(Sum.class) //sum must be applied as last number operator
                    .add(Modulo.class)
                    .add(Multiply.class)
                    .add(DecimalDivide.class)
                    .add(IntegerDivide.class)
                    .add(Count.class) //count should be after all list filters
                    .add(GreaterThanFilter.class)
                    .add(LesserThanFilter.class)
                    .add(GreaterEqualThanFilter.class)
                    .add(LesserEqualThanFilter.class)
                    .add(EqualFilter.class)
                    .add(KeepHighest.class)
                    .add(KeepLowest.class)
                    //dice should be first
                    .add(AddToList.class)
                    .add(NegateAddRemove.class)
                    .add(Reroll.class)
                    .add(Tag.class)
                    .add(Color.class)
                    .add(ExplodingAddDice.class)
                    .add(ExplodingDice.class)
                    .add(RegularDice.class)
                    .add(FromTo.class) //must be before dice, so it can be used to create lists for custom dice
                    .build();

    public static int getOderNumberOf(Class<? extends Operator> operatorDefinitionClass) {
        return operatorOrderList.indexOf(operatorDefinitionClass);
    }
}

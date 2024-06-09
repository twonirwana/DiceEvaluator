package de.janno.evaluator.dice;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class DieId implements Comparable<DieId> {
    @NonNull
    RollId rollId;
    int dieIndex;
    int reroll;

    public static DieId of(int expressionPositionStartInc, String value, int reEvaluateCounter, int dieIndex, int reroll) {
        return new DieId(RollId.of(ExpressionPosition.of(expressionPositionStartInc, value), reEvaluateCounter), dieIndex, reroll);
    }

    @Override
    public String toString() {
        return rollId + "i" + dieIndex + "r" + reroll;
    }

    @Override
    public int compareTo(DieId o) {
        if (!this.rollId.equals(o.getRollId())) {
            return this.rollId.compareTo(o.getRollId());
        } else if (this.dieIndex != o.getDieIndex()) {
            return Integer.compare(this.dieIndex, o.getDieIndex());
        }
        return Integer.compare(this.reroll, o.reroll);
    }
}
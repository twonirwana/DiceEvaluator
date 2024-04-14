package de.janno.evaluator.dice;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;

public record DieId(@NonNull RollId rollId, int dieIndex, int reroll) {
    public static DieId of(@NonNull RollId rollId, int dieIndex, int reroll) {
        return new DieId(rollId, dieIndex, reroll);
    }

    @VisibleForTesting
    public static DieId of(int expressionPositionStartInc, String value, int reEvaluateCounter, int dieIndex, int reroll) {
        return new DieId(RollId.of(ExpressionPosition.of(expressionPositionStartInc, value), reEvaluateCounter), dieIndex, reroll);
    }

    @Override
    public String toString() {
        return rollId + "i" + dieIndex + "r" + reroll;
    }
}
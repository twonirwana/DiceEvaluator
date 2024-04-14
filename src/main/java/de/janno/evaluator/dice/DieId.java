package de.janno.evaluator.dice;

import lombok.NonNull;

public record DieId(@NonNull RollId rollId, int dieIndex, int reroll) {
    public static DieId of(@NonNull RollId rollId, int dieIndex, int reroll) {
        return new DieId(rollId, dieIndex, reroll);
    }

    public static DieId of(int expressionPositionStartInc, int expressionPositionEndInc, String value, int reEvaluateCounter, int dieIndex, int reroll) {
        return new DieId(RollId.of(ExpressionPosition.of(expressionPositionStartInc, expressionPositionEndInc, value), reEvaluateCounter), dieIndex, reroll);
    }

 //todo to string
}

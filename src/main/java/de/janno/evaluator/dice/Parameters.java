package de.janno.evaluator.dice;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;


@Value
@Builder(toBuilder = true)
public class Parameters {
    @Singular
    List<Operator> operators;
    @Singular
    List<Function> functions;
    @Singular
    List<BracketPair> expressionBrackets;
    @Singular
    List<BracketPair> functionBrackets;
    @Builder.Default
    String separator = ",";
    @Singular
    @NonNull
    List<BracketPair> escapeBrackets;
}

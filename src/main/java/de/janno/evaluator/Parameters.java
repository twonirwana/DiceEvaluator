package de.janno.evaluator;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;


@Value
@Builder(toBuilder = true)
public class Parameters<T> {
    @Singular
    List<Operator<T>> operators;
    @Singular
    List<Function<T>> functions;
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

package de.janno.evaluator.dice;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class RollContext {
    @Getter
    private final Map<String, Roll> variables;
    private final Map<ExpressionPosition, AtomicInteger> reEvaluationNumber;

    public RollContext() {
        this(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    private RollContext(Map<String, Roll> variables, Map<ExpressionPosition, AtomicInteger> reEvaluationNumber) {
        this.variables = variables;
        this.reEvaluationNumber = reEvaluationNumber;
    }


    public int getNextReEvaluationNumber(ExpressionPosition expressionPosition) {
        return reEvaluationNumber.computeIfAbsent(expressionPosition, s -> new AtomicInteger(0)).getAndIncrement();
    }


    public RollContext copy() {
        return new RollContext(new ConcurrentHashMap<>(variables), new ConcurrentHashMap<>(reEvaluationNumber));
    }

    public RollContext copyWithEmptyVariables(){
        return new RollContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(reEvaluationNumber));
    }

    public void merge(RollContext rollContext) {
        this.variables.putAll(rollContext.getVariables());
        this.reEvaluationNumber.putAll(rollContext.reEvaluationNumber);
    }
}

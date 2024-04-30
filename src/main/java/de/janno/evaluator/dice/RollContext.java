package de.janno.evaluator.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class RollContext {

    private final List<String> expressionPrefix = new ArrayList<>();
    private final Map<String, Roll> currentVariables;
    private final Map<ExpressionPosition, AtomicInteger> reEvaluationNumber;

    public RollContext() {
        this(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    private RollContext(Map<String, Roll> variables, Map<ExpressionPosition, AtomicInteger> reEvaluationNumber) {
        this.currentVariables = variables;
        this.reEvaluationNumber = reEvaluationNumber;
    }


    public int getNextReEvaluationNumber(ExpressionPosition expressionPosition) {
        return reEvaluationNumber.computeIfAbsent(expressionPosition, s -> new AtomicInteger(0)).getAndIncrement();
    }


    public RollContext copy() {
        return new RollContext(new ConcurrentHashMap<>(currentVariables), new ConcurrentHashMap<>(reEvaluationNumber));
    }

    public RollContext copyWithEmptyVariables() {
        return new RollContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(reEvaluationNumber));
    }

    public void merge(RollContext rollContext) {
        this.currentVariables.putAll(rollContext.currentVariables);
        this.reEvaluationNumber.putAll(rollContext.reEvaluationNumber);
        this.expressionPrefix.addAll(rollContext.expressionPrefix);
    }

    public void putVariable(String name, Roll roll) {
        this.expressionPrefix.add(roll.getExpression());
        this.currentVariables.put(name, roll);
    }

    public Optional<Roll> getVariable(String name) {
        return Optional.ofNullable(this.currentVariables.get(name));
    }

    public Optional<String> getExpressionPrefixString() {
        if (expressionPrefix.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join(", ", expressionPrefix));
    }

}
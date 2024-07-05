package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import de.janno.evaluator.dice.random.NumberSupplier;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class RollContext {

    private final List<String> expressionPrefix = new ArrayList<>();
    private final Map<String, Roll> currentVariables;
    private final Map<ExpressionPosition, AtomicInteger> reEvaluationNumber;
    @Getter
    private final NumberSupplier numberSupplier;
    private final Map<DieId, RandomElement> randomElements;

    public RollContext(NumberSupplier numberSupplier) {
        this(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), numberSupplier, new ConcurrentHashMap<>());
    }

    private RollContext(Map<String, Roll> variables, Map<ExpressionPosition, AtomicInteger> reEvaluationNumber, NumberSupplier numberSupplier, Map<DieId, RandomElement> randomElements) {
        this.currentVariables = variables;
        this.reEvaluationNumber = reEvaluationNumber;
        this.numberSupplier = numberSupplier;
        this.randomElements = randomElements;
    }

    public int getNextReEvaluationNumber(ExpressionPosition expressionPosition) {
        return reEvaluationNumber.computeIfAbsent(expressionPosition, s -> new AtomicInteger(0)).getAndIncrement();
    }

    public RollContext copy() {
        return new RollContext(new ConcurrentHashMap<>(currentVariables), new ConcurrentHashMap<>(reEvaluationNumber), numberSupplier, new ConcurrentHashMap<>(randomElements));
    }

    public RollContext copyWithEmptyVariables() {
        return new RollContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(reEvaluationNumber), numberSupplier, new ConcurrentHashMap<>(randomElements));
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

    public void addRandomElements(List<RandomElement> uniqueList) {
        uniqueList.forEach(r -> randomElements.put(r.getDieId(), r));
    }

    public ImmutableList<RandomElement> getAllRandomElements() {
        return randomElements.values().stream()
                .sorted(Comparator.comparing(RandomElement::getDieId))
                .collect(ImmutableList.toImmutableList());
    }

}
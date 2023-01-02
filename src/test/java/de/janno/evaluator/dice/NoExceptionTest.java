package de.janno.evaluator.dice;

import de.janno.evaluator.dice.random.RandomNumberSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class NoExceptionTest {
    DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000);

    @ParameterizedTest
    @CsvFileSource(resources = "/expressions.csv", delimiter = '|')
    void csvFileSourceTest(String input) throws ExpressionException {
        underTest.evaluate(input);
    }
}

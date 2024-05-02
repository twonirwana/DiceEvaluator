package de.janno.evaluator.dice;

import de.janno.evaluator.dice.random.RandomNumberSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ProdExampleTest {

    private static void write(String input, String result) throws IOException {
        File outputFile = new File("out.csv");
        outputFile.createNewFile();
        Files.writeString(outputFile.toPath(), "\n" + input + "|" + result, StandardOpenOption.APPEND);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/expressionsAndResults.csv", delimiter = '|', maxCharsPerColumn = 100_000)
    void csvFileSourceTest(String expression, String expected) throws ExpressionException, IOException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate(expression).getRolls();
        //write(expression, res.toString());

        assertThat(res.toString()).isEqualTo(expected);

    }
}

package de.janno.evaluator;

import lombok.NonNull;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BracketsTest {

    static Parameters<Double> params = Parameters.<Double>builder()
            .function(new Function<>("sum", 1, Integer.MAX_VALUE) {
                @Override
                protected @NonNull Double evaluate(@NonNull List<Double> arguments) {
                    return arguments.stream().mapToDouble(Double::doubleValue).sum();
                }
            })
            .operator(new Operator<>("+", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, 1) {
                @Override
                protected @NonNull Double evaluate(@NonNull List<Double> operands) {
                    return operands.get(0) + operands.get(1);
                }
            })
            .expressionBracket(BracketPair.PARENTHESES)
            .expressionBracket(BracketPair.BRACKETS)
            .functionBracket(BracketPair.ANGLES)
            .build();
    static AbstractEvaluator<Double> testEvaluator = new AbstractEvaluator<>(params) {
        @Override
        protected Double toValue(String literal) throws ExpressionException {
            if (!NumberUtils.isParsable(literal)) {
                throw new ExpressionException("Not a number: " + literal);
            }
            return Double.parseDouble(literal);
        }
    };

    private static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("[(0.5)+(0.5)]", 1.0),
                Arguments.of("sum<[2]>", 2.0),
                Arguments.of("sum<1,2,2>", 5.0)
        );
    }

    private static Stream<Arguments> generateErrorData() {
        return Stream.of(
                Arguments.of("{(0.5)+(0.5)}", "Not a number: {"),
                Arguments.of("([0.5)+(0.5)]", "Invalid parenthesis match [)"),
                Arguments.of("sum[0.5]", "A function, in this case 'sum', must be followed a open function bracket: <"),
                Arguments.of("<0.5+2>", "Invalid bracket in expression: <")
        );
    }

    @ParameterizedTest(name = "{index} {0} -> {1}")
    @MethodSource("generateData")
    void testSuccess(String input, double expected) throws ExpressionException {
        List<Double> res = testEvaluator.evaluate(input);
        assertThat(res.stream().mapToDouble(d -> d).sum()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{index} {0} -> {1}")
    @MethodSource("generateErrorData")
    void testError(String input, String expectedMessage) {
        assertThatThrownBy(() -> testEvaluator.evaluate(input))
                .isInstanceOf(ExpressionException.class)
                .hasMessage(expectedMessage);
    }
}

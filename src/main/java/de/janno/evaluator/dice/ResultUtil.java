package de.janno.evaluator.dice;

import java.util.List;
import java.util.stream.Collectors;

public final class ResultUtil {

    public static String getExpression(String name, List<Result> arguments) {
        return "%s(%s)".formatted(name, arguments.stream().map(Result::getExpression).collect(Collectors.joining(",")));
    }


    public static String getBinaryOperatorExpression(String name, List<Result> operands) {
        return String.format("%s%s%s", operands.get(0).getExpression(), name, operands.get(1).getExpression());
    }

    public static String getLeftUnaryExpression(String name, List<Result> operands) {
        return String.format("%s%s", operands.get(0).getExpression(), name);
    }

    public static String getRightUnaryExpression(String name, List<Result> operands) {
        return String.format("%s%s", name, operands.get(0).getExpression());
    }

}

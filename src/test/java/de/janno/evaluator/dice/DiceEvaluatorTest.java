package de.janno.evaluator.dice;

import de.janno.evaluator.dice.random.GivenNumberSupplier;
import de.janno.evaluator.dice.random.RandomNumberSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DiceEvaluatorTest {
    private static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("1d6", List.of(3), List.of(3)),
                Arguments.of("1d6 ", List.of(3), List.of(3)),
                Arguments.of(" 1d6", List.of(3), List.of(3)),
                Arguments.of("1D6", List.of(3), List.of(3)),
                Arguments.of("D6", List.of(3), List.of(3)),
                Arguments.of("d6", List.of(3), List.of(3)),
                Arguments.of("-d6", List.of(3), List.of(-3)),
                Arguments.of("-2d6", List.of(3, 4), List.of(-3, -4)),
                Arguments.of("0d6", List.of(), List.of()),
                Arguments.of("3d6", List.of(3, 4, 5), List.of(3, 4, 5)),
                Arguments.of("3d6c", List.of(3, 4, 5), List.of(3)),
                Arguments.of("3d!6c", List.of(3, 2, 6, 6, 5), List.of(5)),
                Arguments.of("3d6>3", List.of(3, 4, 5), List.of(4, 5)),
                Arguments.of("3d6+6>3", List.of(3, 4, 5), List.of(4, 5, 6)),
                Arguments.of("3d6>3c", List.of(3, 4, 5), List.of(2)),
                Arguments.of("3d6>=3", List.of(1, 3, 4), List.of(3, 4)),
                Arguments.of("3d6>=3c", List.of(1, 3, 4), List.of(2)),
                Arguments.of("3d6<3", List.of(3, 4, 5), List.of()),
                Arguments.of("3d6<3c", List.of(3, 4, 5), List.of(0)),
                Arguments.of("3d6<=3", List.of(1, 3, 4), List.of(1, 3)),
                Arguments.of("3d6<=3c", List.of(1, 3, 4), List.of(2)),
                Arguments.of("3d6==3", List.of(1, 3, 4), List.of(3)),
                Arguments.of("3d6==3c", List.of(1, 3, 4), List.of(1)),
                Arguments.of("1d6+1d4 + 1d5", List.of(3, 4, 5), List.of(3, 4, 5)),
                Arguments.of("1d6+1d4 - 1d5", List.of(3, 4, 5), List.of(3, 4, -5)),
                Arguments.of("1d6 + 3", List.of(3), List.of(3, 3)),
                Arguments.of("3d6 + 2d8", List.of(6, 6, 6, 8, 8), List.of(6, 6, 6, 8, 8)),
                Arguments.of("3d6 - 2d8", List.of(6, 6, 6, 8, 8), List.of(6, 6, 6, -8, -8)),
                Arguments.of("1d6 + 3=", List.of(3), List.of(6)),
                Arguments.of("1d(6 + 3=)", List.of(), List.of(9)),
                Arguments.of("1d(6 + 3)", List.of(), List.of(3)),
                Arguments.of("1d6 * 3", List.of(3), List.of(9)),
                Arguments.of("(1d6 * 3) -3", List.of(3), List.of(9, -3)),
                Arguments.of("1d6 * (3 -3=)", List.of(3), List.of(0)),
                Arguments.of("1d6 - 2", List.of(3), List.of(3, -2)),
                Arguments.of("1d6 / 2", List.of(4), List.of(2)),
                Arguments.of("2d6k1", List.of(3, 4), List.of(4)),
                Arguments.of("2d6l1", List.of(3, 4), List.of(3)),
                Arguments.of("(2d6l1)+2", List.of(3, 4), List.of(3, 2)),
                Arguments.of("d!6", List.of(6, 6, 4), List.of(6, 6, 4)),
                Arguments.of("D!6", List.of(6, 6, 4), List.of(6, 6, 4)),
                Arguments.of("2d!6", List.of(3, 6, 6, 4), List.of(3, 6, 6, 4)),
                Arguments.of("2D!6", List.of(3, 6, 6, 4), List.of(3, 6, 6, 4)),
                Arguments.of("2d!6 + 1d6", List.of(3, 6, 6, 4, 1), List.of(3, 6, 6, 4, 1)),
                Arguments.of("d!!6", List.of(6, 6, 4), List.of(16)),
                Arguments.of("D!!6", List.of(6, 6, 4), List.of(16)),
                Arguments.of("2d!!6", List.of(3, 6, 6, 4), List.of(3, 16)),
                Arguments.of("2D!!6", List.of(3, 6, 6, 4), List.of(3, 16)),
                Arguments.of("2d!!6 + 1d6", List.of(3, 6, 6, 4, 1), List.of(3, 16, 1)),
                Arguments.of("min(2d6= , 3d10=)", List.of(3, 6, 6, 4, 1), List.of(9)),
                Arguments.of("max(2d6= , 3d10=)", List.of(3, 6, 6, 4, 1), List.of(11)),
                Arguments.of("min(2d6=,11)", List.of(3, 6), List.of(9)),
                Arguments.of("max(2d6=,11)", List.of(3, 6), List.of(11)),
                Arguments.of("max(11,11)", List.of(), List.of(11, 11)),
                Arguments.of("min(11,11)", List.of(), List.of(11, 11)),
                Arguments.of("[1/2/3]", List.of(), List.of(1, 2, 3)),
                Arguments.of("d[1/2/3]", List.of(2), List.of(2)),
                Arguments.of("2d[1/2/3]", List.of(2, 1), List.of(2, 1)),
                Arguments.of("10d100", List.of(), List.of(100, 100, 100, 100, 100, 100, 100, 100, 100, 100)),
                Arguments.of("2+-3", List.of(), List.of(2, -3)),
                Arguments.of("6 / 3", List.of(), List.of(2)),
                Arguments.of("-1", List.of(), List.of(-1)),
                Arguments.of("3-1", List.of(), List.of(3, -1)),
                Arguments.of("min((max(((10-6=)*2),7,(15))),19+min(1,5)=)", List.of(), List.of(15)),
                Arguments.of("()", List.of(), List.of()),
                Arguments.of("6 (5)", List.of(), List.of(6, 5)),
                Arguments.of("(1d6) (1d6)", List.of(3, 4), List.of(3, 4)),
                Arguments.of("min(1,min(3+2,2))+-(max(4)*2)", List.of(), List.of(1, -8)),
                Arguments.of("min((10,15),20)", List.of(), List.of(10)),
                Arguments.of("min(10,15),20", List.of(), List.of(10, 20)),
                Arguments.of("1d6,(1d6,1)", List.of(1, 2), List.of(1, 2, 1)),
                Arguments.of("3+2+1+1=", List.of(), List.of(7)),
                Arguments.of("cancel(9+10+1+2,10,1)", List.of(), List.of(9, 2)),
                Arguments.of("cancel(9+10+1+2+1,10,1)", List.of(), List.of(9, 2, 1)),
                Arguments.of("cancel(9+10+1+2+10,10,1)", List.of(), List.of(9, 2, 10)),
                Arguments.of("double(9+10+1,10)", List.of(), List.of(9, 10, 10, 1)),
                Arguments.of("double(9+10+1,10+9)", List.of(), List.of(9, 9, 10, 10, 1)),
                Arguments.of("double(9+10+1,10+9+9)", List.of(), List.of(9, 9, 10, 10, 1)),
                Arguments.of("1d+6", List.of(2), List.of(2)),
                Arguments.of("+6+6", List.of(), List.of(6, 6)),
                Arguments.of("+6*6", List.of(), List.of(36)),
                Arguments.of("+6*+6", List.of(), List.of(36)),
                Arguments.of("+6*-6", List.of(), List.of(-36)),
                Arguments.of("val($1, 3d6) $1", List.of(1, 2, 3), List.of(1, 2, 3)),
                Arguments.of("val($1, 3d6) $1 + $1", List.of(1, 2, 3), List.of(1, 2, 3, 1, 2, 3)),
                Arguments.of("val($1, 3d6) $1, $1", List.of(1, 2, 3), List.of(1, 2, 3, 1, 2, 3)),
                Arguments.of("val($1, 3d6) $1= , $1c", List.of(1, 2, 3), List.of(6, 3)),
                Arguments.of("val($1, 3d6) $1= , ($1>2)c", List.of(1, 2, 3), List.of(6, 1)),
                Arguments.of("val($1, val($2, 3d6) + 7) $1 , $2", List.of(1, 2, 3), List.of(7, 1, 2, 3)),
                Arguments.of("val($1, 3d6) val($2, 7) $1 , $2", List.of(1, 2, 3), List.of(1, 2, 3, 7)),
                Arguments.of("val($1, $2) val($2, $1) $1 , $2", List.of(1, 2, 3), List.of()),
                Arguments.of("val($1, $1) $1", List.of(1, 2, 3), List.of()),
                Arguments.of("val($1, 1d6) 2d6", List.of(1, 2, 3), List.of(2, 3)),
                Arguments.of("val($1,6d6), $1=, ($1>4)c", List.of(1, 2, 3, 4, 5, 6), List.of(21, 2)),
                Arguments.of("val($1, 2d6) val($1, 1d6) $1", List.of(1, 2, 3), List.of(1, 2)), //the $1 in the second val is replaced by the first
                Arguments.of("val(2, 'abc'),d6", List.of(2), List.of(2)), //the replacement happens only in the formular, not in results

                Arguments.of("", null, List.of())

        );
    }

    private static List<String> values(List<Roll> in) {
        return in.stream()
                .flatMap(r -> r.getElements().stream())
                .map(RollElement::getValue)
                .toList();
    }

    private static Stream<Arguments> generateStringDiceData() {
        return Stream.of(
                Arguments.of("d[head/ torso/ left arm/ right arm/ left leg/ right leg]", List.of(3), List.of("left arm")),
                Arguments.of("d[head]", List.of(1), List.of("head")),
                Arguments.of("3d[head/ torso/ left arm/ right arm/ left leg/ right leg]", List.of(3, 2, 1), List.of("left arm", "torso", "head")),
                Arguments.of("3d[head/ torso/ left arm/ right arm/ left leg/ right leg] + 2d6", List.of(3, 2, 1, 4, 5), List.of("left arm", "torso", "head", "4", "5")),
                Arguments.of("1d6 + a + 1", List.of(3), List.of("3", "a", "1")),
                Arguments.of("1d6 + [x/y] + 1", List.of(3), List.of("3", "x", "y", "1")),
                Arguments.of("1d6 + [1d6] + 1", List.of(3), List.of("3", "1d6", "1")),
                Arguments.of("1d6 + [1D6] + 1", List.of(3), List.of("3", "1D6", "1")),
                Arguments.of("3d[10/20/30] + 2d6", List.of(3, 2, 1, 4, 5), List.of("30", "20", "10", "4", "5")),
                Arguments.of("d('head'+'torso'+'left arm'+'right arm'+'left leg'+'right leg')", List.of(3), List.of("left arm")),
                Arguments.of("d('head')", List.of(1), List.of("head")),
                Arguments.of("3d('head'+'torso'+'left arm'+'right arm'+'left leg'+'right leg')", List.of(3, 2, 1), List.of("left arm", "torso", "head")),
                Arguments.of("3d('head'+'torso'+'left arm'+'right arm'+'left leg'+'right leg') + 2d6", List.of(3, 2, 1, 4, 5), List.of("left arm", "torso", "head", "4", "5")),
                Arguments.of("1d6 + (a+y) + 1", List.of(3), List.of("3", "a", "y", "1")),
                Arguments.of("1d6 + '1d6' + 1", List.of(3), List.of("3", "1d6", "1")),
                Arguments.of("1d6 + '1D6' + 1", List.of(3), List.of("3", "1D6", "1")),
                Arguments.of("3d(10+20+30) + 2d6", List.of(3, 2, 1, 4, 5), List.of("30", "20", "10", "4", "5")),
                Arguments.of("ifE(1d6,3,'+3','!3')", List.of(3), List.of("+3")),
                Arguments.of("ifE(1d6,3,'+3')", List.of(2), List.of("2")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4')", List.of(2), List.of("2")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4')", List.of(3), List.of("+3")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4')", List.of(4), List.of("+4")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4','else')", List.of(2), List.of("else")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4','else')", List.of(3), List.of("+3")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4','else')", List.of(4), List.of("+4")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(3), List.of("three")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(2), List.of("two")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(1), List.of("one")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(4), List.of("4")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one','else')", List.of(4), List.of("else")),
                Arguments.of("ifE(1d6,3,'three','not three')", List.of(2), List.of("not three")),
                Arguments.of("ifG(1d6,3,'three','not three')", List.of(5), List.of("three")),
                Arguments.of("ifG(1d6,3,'three','not three')", List.of(2), List.of("not three")),
                Arguments.of("ifG(1d6,3,'three')", List.of(2), List.of("2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2')", List.of(2), List.of("2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2')", List.of(3), List.of(">2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2')", List.of(4), List.of(">3")),
                Arguments.of("ifG(1d6,2,'>2',3,'>3')", List.of(4), List.of(">2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2','else')", List.of(1), List.of("else")),
                Arguments.of("ifL(1d6,3,'three','not three')", List.of(2), List.of("three")),
                Arguments.of("ifL(1d6,3,'three','not three')", List.of(4), List.of("not three")),
                Arguments.of("ifL(1d6,3,'three')", List.of(4), List.of("4")),
                Arguments.of("ifIn(1d6,[2/3],'2or3','!2or3')", List.of(3), List.of("2or3")),
                Arguments.of("ifIn(1d6,2,'2','!2')", List.of(2), List.of("2")),
                Arguments.of("ifIn(1d6,2,'2')", List.of(5), List.of("5")),
                Arguments.of("ifIn(1d6,[2/3],'2or3','!2or3')", List.of(4), List.of("!2or3")),
                Arguments.of("replace(8d10, [9/10], 'bonus')", List.of(9, 10, 3, 4, 5, 6, 7, 1), List.of("bonus", "bonus", "3", "4", "5", "6", "7", "1")),
                Arguments.of("replace(8d10, [9/10], [a/a])", List.of(9, 10, 3, 4, 5, 6, 7, 1), List.of("a", "a", "a", "a", "3", "4", "5", "6", "7", "1")),
                Arguments.of("replace(8d10, [9/10], 'bonus')", List.of(1, 2, 3, 4, 5, 6, 7, 8), List.of("1", "2", "3", "4", "5", "6", "7", "8")),
                Arguments.of("[b/2/a]k2", List.of(), List.of("b", "a")),
                Arguments.of("[b/2/a]l2", List.of(), List.of("2", "a")),
                Arguments.of("3.5+2.5", List.of(), List.of("3.5", "2.5")),
                Arguments.of("concat('Attack: ', 3d6) ", List.of(1, 2, 3), List.of("Attack: 1, 2, 3")),
                Arguments.of("concat('Attack: ', 1d20, ' Damage: ', 2d10+5=) ", List.of(1, 2, 3), List.of("Attack: 1 Damage: 10")),
                Arguments.of("val(1, ('a'+'b'+'c')) 3d1", List.of(1, 2, 3), List.of("a", "b", "c")),

                Arguments.of("3x1d6", List.of(1,2,3), List.of("1","2","3")),
                Arguments.of("val($1,1d6) 3x$1", List.of(1,2,3), List.of("1","1","1")),
                Arguments.of("3x(val($1,1d6)+$1)", List.of(1,2,3), List.of("1","2","3")),
                Arguments.of("min(3x1d6)", List.of(1,2,3), List.of("1")),
                Arguments.of("rerollOn(1d6, 1)", List.of(1,2), List.of("2")),
                Arguments.of("rerollOn(1d6, 1)", List.of(1,1), List.of("1")),
                Arguments.of("rerollOn(2d6, [1/5])", List.of(3,5,3,4), List.of("3","4")),


                //Exalted 3e
                Arguments.of("val($1, cancel(double(10d10,10),1,[7/8/9/10])), ifE(($1>=7)c,0,ifG(($1<=1)c,0,'Botch'))", List.of(3, 2, 3, 1, 5, 9, 6, 6, 6, 6, 6), List.of("0")),
                Arguments.of("val($1, cancel(double(10d10,10),1,[7/8/9/10])), ifE(($1>=7)c,0,ifG(($1<=1)c,0,'Botch'))", List.of(3, 2, 3, 3, 5, 9, 6, 6, 6, 6, 6), List.of("1")),
                Arguments.of("val($1, cancel(double(10d10,10),1,[7/8/9/10])), ifE(($1>=7)c,0,ifG(($1<=1)c,0,'Botch'))", List.of(3, 2, 1, 3, 5, 9, 10, 6, 6, 6, 6), List.of("2")),
                Arguments.of("val($1, cancel(double(10d10,10),1,[7/8/9/10])), ifE(($1>=7)c,0,ifG(($1<=1)c,0,'Botch'))", List.of(3, 2, 1, 3, 5, 5, 5, 6, 6, 6, 6), List.of("Botch")),

                Arguments.of("1d0", List.of(), List.of())
        );
    }

    private static Stream<Arguments> generateColorDiceData() {
        return Stream.of(
                Arguments.of("color(3d6,'red')", List.of(3, 2, 1), List.of("3", "2", "1"), List.of("red", "red", "red")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))c", List.of(), List.of("3", "3"), List.of("red", "black")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))=", List.of(), List.of("18", "12"), List.of("red", "black")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))k2", List.of(1, 2, 3, 2, 2, 2), List.of("3", "2", "2", "2"), List.of("red", "red", "black", "black")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))l2", List.of(1, 2, 3, 2, 2, 2), List.of("1", "2", "2", "2"), List.of("red", "red", "black", "black")),
                Arguments.of("asc((color(3d6,'red')+color(3d4,'black')))", List.of(1, 2, 3, 2, 2, 2), List.of("2", "2", "2", "1", "2", "3"), List.of("black", "black", "black", "red", "red", "red")),
                Arguments.of("desc((color(3d6,'red')+color(3d4,'black')))", List.of(1, 2, 3, 2, 2, 2), List.of("3", "2", "1", "2", "2", "2"), List.of("red", "red", "red", "black", "black", "black"))
        );
    }

    private static Stream<Arguments> generateErrorData() {
        return Stream.of(
                Arguments.of("=1", "Operator = has left associativity but the left value was: empty"),
                Arguments.of("1-", "Operator - has right associativity but the right value was: empty"),
                Arguments.of("1*", "Operator * does not support unary operations"),
                Arguments.of("*1", "Operator * does not support unary operations"),
                Arguments.of("10 5 +", "Operator + has right associativity but the right value was: empty"),
                Arguments.of("10**5", "Operator * does not support unary operations"),
                Arguments.of("3d6==[1/2]", "'==' requires as right a single element but was '[1, 2]'. Try to sum the numbers together like ([1, 2]=)"),
                Arguments.of("min()", "Invalid argument count for min"),
                Arguments.of("min(,2)", "A separator can't be followed by another separator or open bracket"),
                Arguments.of("min(1,)", "argument is missing"),
                Arguments.of("min3(45)", "A function, in this case 'min', must be followed a open function bracket: ("),
                Arguments.of(")", "expression can't start with a close bracket"),
                Arguments.of("(", "Parentheses mismatched"),
                Arguments.of(",3", "expression can't start with a separator"),
                Arguments.of("10*", "Operator * does not support unary operations"),
                Arguments.of("10*2.5", "'*' requires as right input a single integer but was '[2.5]'"),
                Arguments.of("2147483647+1=", "integer overflow"),
                Arguments.of("2147483647*2=", "integer overflow"),
                Arguments.of("1/0", "/ by zero"),
                Arguments.of("color(3d6,[a/b])", "'color' requires as second argument a single element but was '[a, b]'"),
                Arguments.of("ifL(2d6,3,'three','not three')", "'ifL' requires as 1 argument a single element but was '[2, 3]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifIn(2d6,3,'three','not three')", "'ifIn' requires as 1 argument a single element but was '[2, 3]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifL(1d6,2d6,'three','not three')", "'ifL' requires as 2 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifG(1d6,2d6,'three','not three')", "'ifG' requires as 2 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifG(1d6,2d6,'three','not three')", "'ifG' requires as 2 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifG(1d6,6,'three',2d6,'not three')", "'ifG' requires as 4 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("'3''5'", "There need to be an operator or a separator between two values"),
                Arguments.of("1d-1", "Not enough values, [d, D] needs 2"),
                Arguments.of("d-1", "Not enough values, [d, D] needs 1"),
                Arguments.of("d!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("d!!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("1d!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("1d!!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("1001d2", "The number of dice must be less or equal then 1000 but was 1001"),
                Arguments.of("1001d!2", "The number of dice must be less or equal then 1000 but was 1001"),
                Arguments.of("1001d!!2", "The number of dice must be less or equal then 1000 but was 1001"),
                Arguments.of("(-6)d2", "The number of dice can not be negativ but was -6"),
                Arguments.of("(-6)d!2", "The number of dice can not be negativ but was -6"),
                Arguments.of("(-6)d!!2", "The number of dice can not be negativ but was -6"),
                Arguments.of("d'-1'", "Sides of dice to roll must be positive"),
                Arguments.of("val($1,1) val($1,1) val($1,1) $1", "The value name '1' was defined more than once."),
                Arguments.of("11x(1d6)","The number of repeat must between 1-10 but was 11"),
                Arguments.of("0x(1d6)","The number of repeat must between 1-10 but was 0"),
                Arguments.of("3d6x(1d6)","'[X, x]' requires as left input a single integer but was '[2, 3, 1]'. Try to sum the numbers together like (3d6=)"),
                Arguments.of("ax(1d6)","'[X, x]' requires as left input a single integer but was '[a]'"),
                Arguments.of("x(1d6)","Operator [X, x] does not support unary operations"),
                Arguments.of("(3d[a/b/c])=","'=' requires as left input only integers but was '[b, c, a]'"),
                Arguments.of("color(2,'red')*color(2,'black')","'*' requires all elements to be the same color, the colors where '[red, black]'"),
                Arguments.of("(3x2d6)=", "'=' requires as 1 inputs but was '[[2, 3], [1, 4], [1, 1]]'")
                );
    }

    private static Stream<Arguments> generateRandomDiceData() {
        return Stream.of(
                Arguments.of("ifE(1d20,1d20,1d20)", List.of(1, 1, 2), List.of(List.of("1"), List.of("1"), List.of("2"))),
                Arguments.of("ifE(1d20,1d20,1d20)", List.of(3, 2), List.of(List.of("3"), List.of("2"))),
                Arguments.of("ifE(1d20,1d20,1d20,1d20)", List.of(3, 4, 2, 4), List.of(List.of("3"), List.of("4"), List.of("4"))),
                Arguments.of("ifE(1d20,1,2)", List.of(1, 1, 2), List.of(List.of("1"))),
                Arguments.of("ifE(1d20,2,3)", List.of(3, 2), List.of(List.of("3"))),
                Arguments.of("ifE(1d20,4,1d20,4)", List.of(3, 4, 2, 4), List.of(List.of("3"))),
                Arguments.of("ifE(3,4,1d20,4)", List.of(3, 4, 2, 4), List.of()),
                Arguments.of("1d6 + 3d20 + 10", List.of(3, 2, 1, 4), List.of(List.of("3"), List.of("2", "1", "4")))
        );
    }

    private static Stream<Arguments> resultSizeDate() {
        return Stream.of(
                Arguments.of("val(2, 'abc'),d6", 1),
                Arguments.of("1d6,d6", 2),
                Arguments.of("d6", 1),
                Arguments.of("1d6", 1),
                Arguments.of("3x1d6", 3),
                Arguments.of("", 0)
        );
    }

    @Test
    void debug() throws ExpressionException {
        //DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1,2,3,4,5,6,7,8,9,10), 1000);
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6), 1000);

        List<Roll> res = underTest.evaluate("3x($1)");
        System.out.println(res.size());
        System.out.println(res);
        System.out.println(res.stream().flatMap(r -> r.getElements().stream()).map(RollElement::getValue).toList());
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("resultSizeDate")
    void resultSize(String expression, int size) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate(expression);

        assertThat(res).hasSize(size);
    }


    @Test
    void testRegularDieHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("1000d" + (Integer.MAX_VALUE - 1));

        assertThat(res.get(0).getElements()).hasSize(1000);
    }

    @Test
    void testCustomDieHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("1000d[" + IntStream.range(0, 1000).mapToObj(i -> "1").collect(Collectors.joining("/")) + "]");

        assertThat(res.get(0).getElements()).hasSize(1000);
    }

    @Test
    void testExplodingDieMaxHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("1000d!" + (Integer.MAX_VALUE - 1));

        assertThat(res.get(0).getElements()).hasSize(1000);
    }

    @Test
    void testExplodingDieTwoHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("1000d!2");

        assertThat(res.get(0).getElements()).hasSizeGreaterThan(1000);
    }

    @Test
    void testExplodingAddDieMaxHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("1000d!!" + (Integer.MAX_VALUE - 1));

        assertThat(res.get(0).getElements()).hasSize(1000);
    }

    @Test
    void testExplodingAddDieTwoHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("1000d!!2");

        assertThat(res.get(0).getElements()).hasSize(1000);
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("generateData")
    void rollExpression(String diceExpression, List<Integer> diceNumbers, List<Integer> expected) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceNumbers), 1000);
        List<Roll> res = underTest.evaluate(diceExpression);

        assertThat(res.stream().flatMap(r -> r.getElements().stream()).flatMap(e -> e.asInteger().stream())).containsExactlyElementsOf(expected);
    }

    @Test
    void sortAsc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000);

        List<Roll> res = underTest.evaluate("asc(4d20)");

        assertThat(values(res)).containsExactly("1", "3", "12", "20");
    }

    @Test
    void sortDesc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000);

        List<Roll> res = underTest.evaluate("desc(4d20)");

        assertThat(values(res)).containsExactly("20", "12", "3", "1");
    }

    @Test
    void sortAlphaAsc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000);

        List<Roll> res = underTest.evaluate("asc(4d20 + 5a +b)");

        assertThat(values(res)).containsExactly("1", "3", "12", "20", "5a", "b");
    }

    @Test
    void sortAlphaDesc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000);

        List<Roll> res = underTest.evaluate("desc(4d20 + 5a + b)");

        assertThat(values(res)).containsExactly("b", "5a", "20", "12", "3", "1");
    }

    @Test
    void groupCount() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("groupC(4d20 + 10d10 + 3d6 + 10 + color(3d6,'red')+color(3d4,'black'))");

        assertThat(res.stream().flatMap(r -> r.getElements().stream()).map(Object::toString)).containsExactly("11x10", "4x20", "black:3x4", "3x6", "red:3x6");
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("generateStringDiceData")
    void rollStringDiceExpression(String diceExpression, List<Integer> diceNumbers, List<String> expected) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceNumbers), 1000);
        List<Roll> res = underTest.evaluate(diceExpression);

        assertThat(values(res)).containsExactlyElementsOf(expected);
    }

    @Test
    void NotSingleIntegerException() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(4, 1), 1000);
        assertThatThrownBy(() -> underTest.evaluate("2d6 / 3"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("'/' requires as left input a single integer but was '[4, 1]'. Try to sum the numbers together like (2d6=)");
    }

    @Test
    void divisorZero() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000);
        assertThatThrownBy(() -> underTest.evaluate("10 / 0"))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("/ by zero");
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("generateRandomDiceData")
    void getRandomElements(String expression, List<Integer> diceThrows, List<List<String>> expectedRandomElements) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceThrows), 1000);
        List<Roll> res = underTest.evaluate(expression);

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue).toList()))
                .containsExactlyElementsOf(expectedRandomElements);
    }


    @Test
    void getRandomElements_regularDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 4, 4, 5), 1000);
        List<Roll> res = underTest.evaluate("(2d4=)d6");

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()))
                .containsExactly(List.of("1", "2"), List.of("4", "4", "5"));

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(4, 4), List.of(6, 6, 6));
    }

    @Test
    void getRandomElements_customDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 4, 4, 5), 1000);
        List<Roll> res = underTest.evaluate("(2d4=)d[a/b/c/d/e/f]");

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()))
                .containsExactly(List.of("1", "2"), List.of("d", "d", "e"));

        assertThat(res.stream().map(r -> r.getRandomElementsInRoll().toString()))
                .containsExactly("[1∈[1...4], 2∈[1...4]], [d∈[a, b, c, d, e, f], d∈[a, b, c, d, e, f], e∈[a, b, c, d, e, f]]");
    }

    @Test
    void getRandomElements_value() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(5, 10, 10), 1000);
        List<Roll> res = underTest.evaluate("val($1,d100) ifG($1, 95, (d100 + $1=), ifL($1, 6, ($1 - d100=)))");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getElements().stream().map(RollElement::getValue)).containsExactly("-5");

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .flatMap(r -> r.getRandomElements().stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)))
                .containsExactly("5", "10");

    }

    @Test
    void getRandomElements_Repeat() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 4, 5), 1000);
        List<Roll> res = underTest.evaluate("5x1d6");

        assertThat(res).hasSize(5);
        assertThat(res.get(0).getElements().stream().map(RollElement::getValue)).containsExactly("1");
        assertThat(res.get(1).getElements().stream().map(RollElement::getValue)).containsExactly("2");
        assertThat(res.get(2).getElements().stream().map(RollElement::getValue)).containsExactly("3");
        assertThat(res.get(3).getElements().stream().map(RollElement::getValue)).containsExactly("4");
        assertThat(res.get(4).getElements().stream().map(RollElement::getValue)).containsExactly("5");

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .flatMap(r -> r.getRandomElements().stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)))
                .containsExactly("1", "2", "3", "4", "5");

    }

    @Test
    void getRandomElements_explodingDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 4, 4, 5), 1000);
        List<Roll> res = underTest.evaluate("(2d!4=)d!6");

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue).toList()))
                .containsExactly(List.of("1", "2"), List.of("4", "4", "5"));

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(4, 4), List.of(6, 6, 6));
    }

    @Test
    void getRandomElements_explodingAddDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 4, 4, 5), 1000);
        List<Roll> res = underTest.evaluate("(2d!!4=)d!!6");

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()))
                .containsExactly(List.of("1", "2"), List.of("4", "4", "5"));

        assertThat(res.stream().flatMap(r -> r.getRandomElementsInRoll().getRandomElements().stream())
                .map(r -> r.getRandomElements().stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(4, 4), List.of(6, 6, 6));
    }

    @Test
    void toStringTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000);
        List<Roll> res = underTest.evaluate("1d6 + 3d20 + 10 +min(2d6,3d4)");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getRandomElementsString()).isEqualTo("[3] [2, 1, 4] [6, 6] [4, 4, 4]");
        assertThat(res.get(0).getResultString()).isEqualTo("3, 2, 1, 4, 10, 4, 4, 4");
        assertThat(res.get(0).getExpression()).isEqualTo("1d6+3d20+10+min(2d6,3d4)");
    }

    @Test
    void toStringBracketTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000);
        List<Roll> res = underTest.evaluate("3d(20 + 10=)");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getRandomElementsString()).isEqualTo("[3, 2, 1]");
        assertThat(res.get(0).getResultString()).isEqualTo("3, 2, 1");
        assertThat(res.get(0).getExpression()).isEqualTo("3d20+10=");
    }

    @Test
    void toStringColorTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000);
        List<Roll> res = underTest.evaluate("color(1d6,'red') + color(3d20,'blue')");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getRandomElementsString()).isEqualTo("[3] [2, 1, 4]");
        assertThat(res.get(0).getResultString()).isEqualTo("red:3, blue:2, blue:1, blue:4");
        assertThat(res.get(0).getExpression()).isEqualTo("color(1d6,red)+color(3d20,blue)");
    }

    @Test
    void toStringMultiExpressionTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000);
        List<Roll> res = underTest.evaluate("1d6 + 3d20, 10 +min(2d6,3d4)");

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getRandomElementsString()).isEqualTo("[3] [2, 1, 4]");
        assertThat(res.get(0).getResultString()).isEqualTo("3, 2, 1, 4");
        assertThat(res.get(0).getExpression()).isEqualTo("1d6+3d20");
        assertThat(res.get(1).getRandomElementsString()).isEqualTo("[6, 6] [4, 4, 4]");
        assertThat(res.get(1).getResultString()).isEqualTo("10, 4, 4, 4");
        assertThat(res.get(1).getExpression()).isEqualTo("10+min(2d6,3d4)");
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}-{3}")
    @MethodSource("generateColorDiceData")
    void rollStringDiceExpressionWithColor(String diceExpression, List<Integer> diceNumbers, List<String> expectedValues, List<String> expectedColors) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceNumbers), 1000);
        List<Roll> res = underTest.evaluate(diceExpression);

        assertThat(values(res)).containsExactlyElementsOf(expectedValues);
        assertThat(res.stream().flatMap(r -> r.getElements().stream()).map(RollElement::getColor)).containsExactlyElementsOf(expectedColors);
    }

    @Test
    void maxDice() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000);

        assertThatThrownBy(() -> underTest.evaluate("1001d6"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("The number of dice must be less or equal then 1000 but was 1001");
    }

    @Test
    void maxNegativeDice() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000);
        assertThatThrownBy(() -> underTest.evaluate("-1001d6"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("The number of dice must be less or equal then 1000 but was 1001");
    }

    @Test
    void bigResult() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000);

        List<Roll> res = underTest.evaluate("1000d999999999999999999999999999999");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getElements().size()).isEqualTo(1000);
        assertThat(res.get(0).getElements().stream()).allMatch(r -> r.getValue().equals("999999999999999999999999999999"));
    }


    @ParameterizedTest(name = "{index} {0} -> {1}")
    @MethodSource("generateErrorData")
    void testError(String input, String expectedMessage) {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000);
        assertThatThrownBy(() -> underTest.evaluate(input))
                .isInstanceOfAny(ExpressionException.class, ArithmeticException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void rollTwice_1d6() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2), 1000);

        Roller res = underTest.buildRollSupplier("1d6");

        assertThat(res.roll().get(0).getElements().toString()).isEqualTo("[1]");
        assertThat(res.roll().get(0).getElements().toString()).isEqualTo("[2]");
    }

    @Test
    void rollTwice_value() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 2, 3, 4), 1000);

        Roller res = underTest.buildRollSupplier("val($1, 3d6), (($1)=) + (($1>2)c)");


        assertThat(res.roll().get(0).getElements().toString()).isEqualTo("[6, 1]");
        assertThat(res.roll().get(0).getElements().toString()).isEqualTo("[9, 2]");
    }

    @Test
    void testHelp() {
        assertThat(DiceEvaluator.getHelpText())
                .contains("Regular Dice");
    }
}
